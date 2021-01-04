package com.arpit.notify.asyncTasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.arpit.notify.database.NotesDatabase;
import com.arpit.notify.entities.Note;

@SuppressWarnings("ALL")
public class UnArchiveNoteAsync extends AsyncTask<Void,Void,Void> {

    @SuppressLint("StaticFieldLeak")
    Context context;
    Note note;

    public UnArchiveNoteAsync(Context context, Note note) {
        this.context = context;
        this.note = note;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        note.setArch(false);
        NotesDatabase.getDatabase(context).noteDao().insertNote(note);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
