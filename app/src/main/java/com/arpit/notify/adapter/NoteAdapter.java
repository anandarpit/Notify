package com.arpit.notify.adapter;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arpit.notify.R;
import com.arpit.notify.entities.Note;
import com.arpit.notify.listeners.NotesListeners;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.myAdapter> {

    private List<Note> list;
    private NotesListeners notesListeners;
    private Timer timer;
    private List<Note> noteSource;

    public NoteAdapter(List<Note> list, NotesListeners notesListeners) {
        this.list = list;
        this.notesListeners = notesListeners;
        noteSource = list;
    }

    @NonNull
    @Override
    public NoteAdapter.myAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_note,parent,
                        false);
        return new myAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.myAdapter holder, int position) {
        holder.bind(list.get(position));
        holder.layoutContaier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListeners.onNoteClicked(list.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class myAdapter extends RecyclerView.ViewHolder {

        TextView textTitle, textSubtitle, textDate;
        LinearLayout layoutContaier;
        RoundedImageView noteImage;
        private Timer timer;

        public myAdapter(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDate = itemView.findViewById(R.id.textDate);
            layoutContaier = itemView.findViewById(R.id.item_container_layout);
            noteImage = itemView.findViewById(R.id.note_image);

        }

        public void bind(Note note)
        {

            textTitle.setText(note.getTitle());
            textDate.setText(note.getDateTime());

            if(note.getSubtitle().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }
            else{
                textSubtitle.setText(note.getSubtitle());
            }

            GradientDrawable gradientDrawable = (GradientDrawable) layoutContaier.getBackground();

            if(note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if(note.getImagePath() != null){
                noteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                noteImage.setVisibility(View.VISIBLE);
            }
            else{
                noteImage.setVisibility(View.GONE);
            }

        }
    }
    public void searchNotes(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()) {
                    list = noteSource;
                }
                else{
                    ArrayList<Note> temp = new ArrayList<>();
                    for(Note note: noteSource){
                        if(note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        ||note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                        ||note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    list = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        } ,10);
    }
    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }
}