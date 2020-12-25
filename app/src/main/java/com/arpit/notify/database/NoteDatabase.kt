package com.arpit.notify.database

import androidx.room.Database
import com.arpit.notify.entities.Note


@Database(entities = arrayOf(Note::class), version = 1, exportSchema = false )
class NoteDatabase {
}