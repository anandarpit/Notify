package com.arpit.notify.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arpit.notify.asyncTasks.ArchiveNoteAsync
import com.arpit.notify.R
import com.arpit.notify.asyncTasks.UnArchiveNoteAsync
import com.arpit.notify.adapter.NoteAdapter
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import com.arpit.notify.listeners.NotesListeners
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NotesListeners  {
    var myList: MutableList<Note> = mutableListOf<Note>()
    lateinit var noteAdapter: NoteAdapter

    val REQUEST_CODE_ADD_NOTE = 1
    val REQUEST_CODE_UPDATE_NOTE = 2
    val REQUEST_CODE_SHOW_NOTES = 3

    private var noteClickedPosition = -1
    var pos = -1
    var deleteItem: Note? = null

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
                if (myList.size != 0)
                    noteAdapter.searchNotes(p0.toString())
            }

        })


        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        showmenu.setOnClickListener{
            showPopup(showmenu)
        }
    }

    fun showPopup(v: View){
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.main_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.archives -> {

                }
                R.id.backup -> {

                }
                R.id.importnotes -> {

                }
            }
            true
        }
        popup.show()
    }

    private val itemTouchHelperCallback =
            object :
                    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT
//                            or ItemTouchHelper.RIGHT
                    ) {
                override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                ): Boolean {
                    // Code for movement
                    return false
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    pos = viewHolder.adapterPosition
                    deleteItem = myList.get(pos)
                    if(direction == ItemTouchHelper.LEFT){
                        myList.removeAt(pos)
                        noteAdapter.notifyDataSetChanged()

                        val snackbar = Snackbar.make(mainactivity, "Note Archived",
                                Snackbar.LENGTH_LONG).setAction("UNDO") {

                            if(pos!=-1){
                                myList.add(pos, deleteItem!!)
                                noteAdapter.notifyItemInserted(pos)
                                val unarchiveNoteAsync = UnArchiveNoteAsync(applicationContext, deleteItem)
                                unarchiveNoteAsync.execute()
                            }
                        }
                        snackbar.setActionTextColor(Color.RED)
                        val snackbarView = snackbar.view
                        snackbarView.setBackgroundColor(Color.parseColor("#333333"))
                        snackbar.show()
                        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {

                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                if(event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT){
                                    val archiveNoteAsync = ArchiveNoteAsync(applicationContext, deleteItem)
                                    archiveNoteAsync.execute()
                                }
                            }
                        })

                    }
                }

            }


    private fun getNote(requestCode: Int, isNoteDeleted: Boolean) {
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask: AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg p0: Void?): List<Note>? {
                return NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes()
            }

            override fun onPostExecute(rawResult: List<Note>?) {
                super.onPostExecute(rawResult)
                var result = ArrayList<Note>()
                for (not in rawResult!!) {
                    if(not.arch != null) {
                        if (!not.arch) {
                            result.add(not)
                            Log.d("xx",not.toString())
                        }
                    }
                    else if(not.arch == null){
                        result.add(not)
                        Log.d("xx",not.toString())
                    }
                }
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
                    }
                    noteAdapter.notifyItemInserted(0)
                    recyclerView.smoothScrollToPosition(0)
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
                    getNote(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false))
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