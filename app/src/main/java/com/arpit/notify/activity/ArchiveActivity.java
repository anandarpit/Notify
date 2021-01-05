package com.arpit.notify.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.airbnb.lottie.LottieAnimationView;
import com.arpit.notify.R;
import com.arpit.notify.adapter.NoteAdapter;
import com.arpit.notify.asyncTasks.ArchiveNoteAsync;
import com.arpit.notify.asyncTasks.UnArchiveNoteAsync;
import com.arpit.notify.database.NotesDatabase;
import com.arpit.notify.entities.Note;
import com.arpit.notify.listeners.NotesListeners;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ArchiveActivity extends AppCompatActivity implements NotesListeners {

    List<Note> myList = new ArrayList<>();
    NoteAdapter noteAdapter;
    RecyclerView recyclerView;
    ConstraintLayout constraintLayout;
    EditText search;
    int noteClickedPosition = -1;
    int flag =0;

    int REQUEST_CODE_UPDATE_NOTE = 2;
    int REQUEST_CODE_SHOW_NOTES = 3;

    int pos = -1;
    Note unArchiveNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        recyclerView = findViewById(R.id.recyclerView);
        search = findViewById(R.id.searchArchive);
        constraintLayout = findViewById(R.id.archiveactivity);

        getArchivedNotes(REQUEST_CODE_SHOW_NOTES,false);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        noteAdapter = new NoteAdapter(myList,this);
        recyclerView.setAdapter(noteAdapter);

        ItemTouchHelper.Callback itemTouchHelperCallback;
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(myList.size() != 0){
                    noteAdapter.searchNotes(editable.toString());
                }
            }
        });


    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() { return true; }
        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {return 50000f;}
        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {return 0.6f;}

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            pos = viewHolder.getAdapterPosition();
            unArchiveNote = myList.get(pos);
            myList.remove(pos);
            noteAdapter.notifyDataSetChanged();

            Snackbar snack = Snackbar.make(constraintLayout, "Removing from Archives...", 1800 )
                    .setAction("CANCEL", view -> {
                        if(pos!=-1){
                            myList.add(pos, unArchiveNote);
                            noteAdapter.notifyItemInserted(pos);
                            ArchiveNoteAsync archiveNoteAsync = new ArchiveNoteAsync(getApplicationContext(), unArchiveNote);
                            archiveNoteAsync.execute();
                        }
                    });
            snack.setActionTextColor(Color.RED);
            View view = snack.getView();
            view.setBackgroundColor(Color.parseColor("#333333"));
            snack.show();
            snack.addCallback(new Snackbar.Callback() {

                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {

                        UnArchiveNoteAsync unArchiveNoteAsync = new UnArchiveNoteAsync(getApplicationContext(),unArchiveNote);
                        unArchiveNoteAsync.execute();
                        flag = 1;
                        Snackbar.make(constraintLayout, "Unarchived!",
                                700).show();
                    }
                }
            });
        }
    };

    private void getArchivedNotes(int requestCode, Boolean isNoteDeleted) {
        class GetArchiveNotesTask extends AsyncTask<Void,Void,List<Note>>{


            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(List<Note> result) {
                super.onPostExecute(result);
                List<Note> notes = new ArrayList();
                for (Note note: result) {
                    if(note.getArch()  != null) {
                        if (note.getArch()) {
                            notes.add(note);
                        }
                    }
                }

                if(requestCode == REQUEST_CODE_SHOW_NOTES){
                    myList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(0);
                }
                if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                    myList.remove(noteClickedPosition);

                    if(isNoteDeleted){
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    }
                    else{
                        myList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);
                        recyclerView.smoothScrollToPosition(noteClickedPosition);
                    }
                }
            }
        }

        GetArchiveNotesTask getArchiveNotesTask = new GetArchiveNotesTask();
        getArchiveNotesTask.execute();
    }

    @Override
    public void onBackPressed() {
        Intent intent =new Intent();
        intent.putExtra("flagUnarchive",flag);
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(this, CreateNotesActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        intent.putExtra("fromArchive",true);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    @Override
    public void onNoteLongedClicked(Note note, int position) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_NOTE) {
            if (data != null) {
                getArchivedNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }
}