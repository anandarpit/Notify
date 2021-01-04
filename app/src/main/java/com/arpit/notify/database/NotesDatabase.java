package com.arpit.notify.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.arpit.notify.dao.NoteDao;
import com.arpit.notify.entities.Note;

@Database(entities = Note.class, version = 2, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    private static NotesDatabase notesDatabase;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE note ADD COLUMN archived INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static synchronized NotesDatabase getDatabase(Context context)
    {
        if(notesDatabase == null){
           notesDatabase =  Room.databaseBuilder(context
            , NotesDatabase.class,
                    "note_db"
                    )
                   .addMigrations(MIGRATION_1_2)
                   .build();
        }
        return notesDatabase;
    }


    public abstract NoteDao noteDao(); // needs to have abstract method related to each dao that you have created here only 1 I have created
}