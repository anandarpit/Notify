package com.arpit.notify.listeners;

import com.arpit.notify.entities.Note;

public interface NotesListeners {
    void onNoteClicked(Note note, int position);

}
