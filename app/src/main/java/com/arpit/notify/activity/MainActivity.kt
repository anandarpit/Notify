package com.arpit.notify.activity

import android.R.attr
import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arpit.notify.R
import com.arpit.notify.adapter.NoteAdapter
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import com.arpit.notify.listeners.NotesListeners
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NotesListeners  {
    var myList: MutableList<Note> = mutableListOf<Note>()
    lateinit var noteAdapter: NoteAdapter

    val REQUEST_CODE_ADD_NOTE = 1
    val REQUEST_CODE_UPDATE_NOTE = 2
    val REQUEST_CODE_SHOW_NOTES = 3
    private var noteClickedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        add_note.setOnClickListener{
            val intent = Intent(this, CreateNotesActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }

        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        noteAdapter = NoteAdapter(myList, this)
        recyclerView.adapter = noteAdapter
        getNote(REQUEST_CODE_SHOW_NOTES)
    }

    private fun getNote(requestCode: Int) {
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask: AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg p0: Void?): List<Note>? {
                return NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes()
            }

            override fun onPostExecute(result: List<Note>?) {
                super.onPostExecute(result)
                if(requestCode == REQUEST_CODE_SHOW_NOTES){
                    if (result != null) {
                        myList.addAll(result)
                    }
                    noteAdapter.notifyDataSetChanged()
                    recyclerView.smoothScrollToPosition(0)
                }
                else if(requestCode==REQUEST_CODE_ADD_NOTE){
                    if (result != null) {
                        myList.add(0, result.get(0))
                        noteAdapter.notifyItemInserted(0)
                        recyclerView.smoothScrollToPosition(0)
                    }
                }
                else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                    Log.d("noteposition",noteClickedPosition.toString())
                    myList.removeAt(noteClickedPosition)
                    if (result != null) {
                        myList.add(noteClickedPosition, result.get(noteClickedPosition))
                        noteAdapter.notifyItemChanged(noteClickedPosition)
                        recyclerView.smoothScrollToPosition(noteClickedPosition)
                    }
                }
            }
        }

        GetNoteTask().execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ADD_NOTE) {
                getNote(REQUEST_CODE_ADD_NOTE)
            } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_NOTE) {
                if (data != null) {
                    getNote(REQUEST_CODE_UPDATE_NOTE)
                }
            }

    }

    override fun onNoteClicked(note: Note?, position: Int) {
        noteClickedPosition = position
        val intent = Intent(this, CreateNotesActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)
    }
}
