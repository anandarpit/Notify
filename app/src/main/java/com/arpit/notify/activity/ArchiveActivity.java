package com.arpit.notify.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.arpit.notify.R;
import com.arpit.notify.adapter.NoteAdapter;
import com.arpit.notify.database.NotesDatabase;
import com.arpit.notify.entities.Note;
import com.arpit.notify.listeners.NotesListeners;

import java.util.ArrayList;
import java.util.List;

public class ArchiveActivity extends AppCompatActivity implements NotesListeners {

    List<Note> myList = new ArrayList<>();
    NoteAdapter noteAdapter;
    RecyclerView recyclerView;
    int noteClickedPosition = -1;

    int REQUEST_CODE_ADD_NOTE = 1;
    int REQUEST_CODE_UPDATE_NOTE = 2;
    int REQUEST_CODE_SHOW_NOTES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        recyclerView = findViewById(R.id.recyclerView);

        getArchivedNotes(REQUEST_CODE_SHOW_NOTES,false);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        noteAdapter = new NoteAdapter(myList,this);
        recyclerView.setAdapter(noteAdapter);

    }

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_NOTE) {
            if (data != null) {
                getArchivedNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }
}