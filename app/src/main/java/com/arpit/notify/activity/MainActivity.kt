@file:Suppress("DEPRECATION")

package com.arpit.notify.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arpit.notify.R
import com.arpit.notify.adapter.NoteAdapter
import com.arpit.notify.asyncTasks.ArchiveNoteAsync
import com.arpit.notify.asyncTasks.UnArchiveNoteAsync
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import com.arpit.notify.listeners.NotesListeners
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NotesListeners {
    var myList: MutableList<Note> = mutableListOf<Note>()
    lateinit var noteAdapter: NoteAdapter

    val REQUEST_CODE_ADD_NOTE = 1
    val REQUEST_CODE_UPDATE_NOTE = 2
    val REQUEST_CODE_SHOW_NOTES = 3
    val REQUEST_CODE_UNARCHIVED_NOTE = 4

    private var noteClickedPosition = -1
    var pos = -1
    var archiveNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getNote(REQUEST_CODE_SHOW_NOTES, false)

        add_note.setOnClickListener{
            val intent = Intent(this, CreateNotesActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }
        val layoutmanager = StaggeredGridLayoutManager(if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2, StaggeredGridLayoutManager.VERTICAL)
        layoutmanager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutmanager
        noteAdapter = NoteAdapter(myList, this)
        recyclerView.adapter = noteAdapter

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
                    val intent = Intent(this, ArchiveActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_UNARCHIVED_NOTE)
                    overridePendingTransition(0, 0)
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
                    ItemTouchHelper.SimpleCallback(0, 0
                    ) {
                var dragFrom = -1
                var dragTo = -1

                override fun isLongPressDragEnabled(): Boolean { return true}
                override fun isItemViewSwipeEnabled(): Boolean { return true}
                override fun getSwipeEscapeVelocity(defaultValue: Float): Float { return 55500f }
                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float { return 0.6f }

                override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
//                    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
//                            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    val dragFlags = 0
                    val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                    return makeMovementFlags(dragFlags, swipeFlags)
                }

                override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition

                    if(dragFrom == -1) {
                        dragFrom =  fromPosition
                    }
                    dragTo = toPosition

                    return false
                }


                private fun reallyMoved(from: Int, to: Int) {
                    Collections.swap(myList, from, to)
                    noteAdapter.notifyItemMoved(from, to)
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView!!, viewHolder!!)
                    if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                        reallyMoved(dragFrom, dragTo)
                    }
                    dragTo = -1
                    dragFrom = dragTo
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    pos = viewHolder.adapterPosition
                    archiveNote = myList.get(pos)
                    myList.removeAt(pos)
                    noteAdapter.notifyDataSetChanged()

                    val archiveNoteAsync = ArchiveNoteAsync(applicationContext, archiveNote)
                    archiveNoteAsync.execute()

                    val snackbar = Snackbar.make(mainactivity, "Archived!",
                            1800).setAction("UNDO") {

                        if(pos!=-1){
                            myList.add(pos, archiveNote!!)
                            noteAdapter.notifyItemInserted(pos)
                            val unarchiveNoteAsync = UnArchiveNoteAsync(applicationContext, archiveNote)
                            unarchiveNoteAsync.execute()
                        }
                    }
                    snackbar.setActionTextColor(Color.RED)
                    val snackbarView = snackbar.view
                    snackbarView.setBackgroundColor(Color.parseColor("#333333"))
                    snackbar.show()
                }
            }



    private fun getNote(requestCode: Int, isNoteDeleted: Boolean) {
        @Suppress("DEPRECATION")
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask: AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg p0: Void?): List<Note>? {
                return NotesDatabase.getDatabase(applicationContext).noteDao().getAllNotes()
            }

            override fun onPostExecute(rawResult: List<Note>?) {
                super.onPostExecute(rawResult)
                var result = ArrayList<Note>()
                result.clear()
                for (not in rawResult!!) {
                    if(not.arch != null) {
                        if (!not.arch) {
                            result.add(not)
                        }
                    }
                    else if(not.arch == null){
                        result.add(not)
                    }
                }
                if(requestCode == REQUEST_CODE_SHOW_NOTES){
                    myList.clear()
                    myList.addAll(result)
                    noteAdapter.notifyDataSetChanged()
                    recyclerView.smoothScrollToPosition(0)
                }
                else if(requestCode==REQUEST_CODE_ADD_NOTE){
                    myList.add(0, result.get(0))
                    noteAdapter.notifyItemInserted(0)
                    recyclerView.smoothScrollToPosition(0)
                }
                else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                    myList.removeAt(noteClickedPosition)

                    if(isNoteDeleted){
                        noteAdapter.notifyDataSetChanged()
                    }
                    else{
                        myList.add(noteClickedPosition, result.get(noteClickedPosition))
                        noteAdapter.notifyDataSetChanged()
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
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_CODE_UNARCHIVED_NOTE){
            if(data != null){
                if(data.getIntExtra("flagUnarchive",0) == 1){
                    getNote(REQUEST_CODE_SHOW_NOTES,false)
                }
            }
        }

    }

    override fun onNoteClicked(note: Note?, position: Int) {
        noteClickedPosition = position
        val intent = Intent(this, CreateNotesActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)
        intent.putExtra("fromArchive", false)
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)
    }

    override fun onNoteLongedClicked(note: Note?, position: Int) {
        //Let it be empty
    }

}