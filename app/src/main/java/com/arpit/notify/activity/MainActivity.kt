package com.arpit.notify.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arpit.notify.R
import com.arpit.notify.adapter.NoteAdapter
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var myList: MutableList<Note> = mutableListOf<Note>()
    lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        add_note.setOnClickListener{
            val intent = Intent(this, CreateNotesActivity::class.java)
            startActivityForResult(intent, 10000)
        }

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        noteAdapter = NoteAdapter(myList)
        recyclerView.adapter = noteAdapter

        getNote()
    }

    private fun getNote() {
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask: AsyncTask<Void,Void,List<Note>>() {
            override fun doInBackground(vararg p0: Void?): List<Note>? {
                return NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes()
            }

            override fun onPostExecute(result: List<Note>?) {
                super.onPostExecute(result)
                if(myList.isEmpty()){
                    if (result != null) {
                        myList.addAll(result)
                        noteAdapter.notifyDataSetChanged()
                    }
                }
                else{
                    if (result != null) {
                        myList.add(0,result.get(0))
                        noteAdapter.notifyItemInserted(0)

                    }
                }
                recyclerView.smoothScrollToPosition(0)
            }
        }

        GetNoteTask().execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode ==10000){
            getNote()
        }
    }
}