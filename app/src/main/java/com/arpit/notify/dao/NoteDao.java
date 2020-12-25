package com.arpit.notify.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.arpit.notify.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM note ORDER BY id DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void delete(Note note);
}
