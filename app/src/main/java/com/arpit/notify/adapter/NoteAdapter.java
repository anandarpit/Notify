package com.arpit.notify.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.myAdapter> {

    private List<Note> list;
    private final NotesListeners notesListeners;
    private Timer timer;
    private final List<Note> noteSource;

    public NoteAdapter(List<Note> liste, NotesListeners notesListeners) {
        this.list = liste;
        this.notesListeners = notesListeners;
        noteSource = liste;
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
        holder.setIsRecyclable(false);
        holder.layoutContainer.setOnClickListener(view -> notesListeners.onNoteClicked(list.get(position),position));
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
        LinearLayout layoutContainer;
        RoundedImageView noteImage;

        public myAdapter(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDate = itemView.findViewById(R.id.textDate);
            layoutContainer = itemView.findViewById(R.id.item_container_layout);
            noteImage = itemView.findViewById(R.id.note_image);

        }

        public void bind(Note note)
        {
            textDate.setText(note.getDateTime());

            if( note.getTitle() != null) {
                if (note.getTitle().isEmpty()) {
                    textTitle.setVisibility(View.GONE);
                } else {
                    textTitle.setText(note.getTitle());
                }
            }

            if( note.getSubtitle() != null){
                if(note.getSubtitle().isEmpty()){
                    textSubtitle.setVisibility(View.GONE);
                }
                 else{
                    textSubtitle.setText(note.getSubtitle());
                }
            }

            GradientDrawable gradientDrawable = (GradientDrawable) layoutContainer.getBackground();

            if(note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }
            else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if(note.getImagePath() != null){

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(note.getImagePath(),bmOptions);

                noteImage.setImageBitmap(bitmap);
                noteImage.setVisibility(View.VISIBLE);
                Picasso.get().load(note.getImagePath()).into(noteImage);
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
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        } ,50);
    }
    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }
}