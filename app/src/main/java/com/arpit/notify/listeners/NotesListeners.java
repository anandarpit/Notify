package com.arpit.notify.listeners;

import com.arpit.notify.entities.Note;

public interface NotesListeners {
    void onNoteClicked(Note note, int position);
    void onNoteSelected(Note note, int position);
    void onNoteLongItemClicked(Note note, int position);
}
