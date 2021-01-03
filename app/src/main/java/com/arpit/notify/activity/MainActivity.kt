package com.arpit.notify.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arpit.notify.R
import com.arpit.notify.adapter.NoteAdapter
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import com.arpit.notify.listeners.NotesListeners
import com.google.android.material.snackbar.Snackbar
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
        getNote(REQUEST_CODE_SHOW_NOTES, false)




        searchNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteAdapter.cancelTimer()
            }

            override fun afterTextChanged(p0: Editable?) {
               if(myList.size != 0)
                   noteAdapter.searchNotes(p0.toString())
            }

        })

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private val itemTouchHelperCallback =
            object :
                    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                ): Boolean {
                    // Code for movement
                    return false
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val pos = viewHolder.adapterPosition
                    val deleteItem = myList.get(pos)
                   // Code for swipe behavior
                    if(direction == ItemTouchHelper.LEFT){
                        myList.removeAt(pos)
                        noteAdapter.notifyDataSetChanged()
                        onSNACK(mainactivity , "Note Archived")
                    }
//                    if(direction == ItemTouchHelper.RIGHT){
//                    }
                }

            }

    fun onSNACK(view: View, text: String){
        val snackbar = Snackbar.make(view, text,
                Snackbar.LENGTH_LONG).setAction("UNDO", View.OnClickListener {

        })
        snackbar.setActionTextColor(Color.RED)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.parseColor("#333333"))
        snackbar.show()
    }

//    noteViewModel.delete(noteAdapter.getNoteAt(viewHolder.adapterPosition))
//    Toast.makeText(
//    this@MainActivity,
//    getString(R.string.note_deleted),
//    Toast.LENGTH_SHORT
//    ).show()


    private fun getNote(requestCode: Int, isNoteDeleted: Boolean) {
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
                    myList.removeAt(noteClickedPosition)

                    if(isNoteDeleted){
                        noteAdapter.notifyItemRemoved(noteClickedPosition)
                    }
                    else{
                        if (result != null) {
                            myList.add(noteClickedPosition, result.get(noteClickedPosition))
                        }
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
                getNote(REQUEST_CODE_ADD_NOTE, false)
            } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_NOTE) {
                if (data != null) {
                    getNote(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted",false))
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

    override fun onNoteSelected(note: Note?, position: Int) {
        TODO("Not yet implemented")
    }

    override fun onNoteLongItemClicked(note: Note?, position: Int) {
        TODO("Not yet implemented")
    }
}