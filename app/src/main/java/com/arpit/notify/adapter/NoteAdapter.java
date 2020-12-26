package com.arpit.notify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arpit.notify.R;
import com.arpit.notify.entities.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.myAdapter> {

    private List<Note> list;

    public NoteAdapter(List<Note> list) {
        this.list = list;
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

        public myAdapter(@NonNull View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.textTitle);
            textSubtitle = itemView.findViewById(R.id.textSubtitle);
            textDate = itemView.findViewById(R.id.textDate);

        }

        public void bind(Note note) {

            textTitle.setText(note.getTitle());

            textDate.setText(note.getDateTime());
            if(note.getSubtitle().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }
            else{
                textSubtitle.setText(note.getSubtitle());
            }

        }
    }
}
