package com.arpit.notify.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.arpit.notify.dao.NoteDao;
import com.arpit.notify.entities.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase notesDatabase;

    public static synchronized NotesDatabase getDatabase(Context context)
    {
        if(notesDatabase == null){
           notesDatabase =  Room.databaseBuilder(context
            , NotesDatabase.class,
                    "note_db"
                    ).build();
        }
        return notesDatabase;
    }

    public abstract NoteDao noteDao(); // needs to have abstract method related to each dao that you have created here only 1 I have created
}