package com.arpit.notify.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.arpit.notify.R
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        getNote()
        add_note.setOnClickListener{
            val intent = Intent(this, CreateNotesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getNote() {
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask: AsyncTask<Void,Void,List<Note>>() {
            override fun doInBackground(vararg p0: Void?): List<Note>? {
                if(!NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes().isEmpty())
                {
                    return NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes()
                }

              return  null
            }

            override fun onPostExecute(result: List<Note>?) {
                super.onPostExecute(result)
                Log.d("MainActvity", result.toString())
            }
        }

        GetNoteTask().execute()
    }
}