package com.arpit.notify.dao;


import androidx.annotation.IdRes;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.arpit.notify.entities.Note;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM note ORDER BY id DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(Note note);

    @Delete
    void delete(Note note);



}
