package com.arpit.notify.activity

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arpit.notify.R
import com.arpit.notify.adapter.NoteAdapter
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_create_notes.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_miscellaneous.*
import java.text.SimpleDateFormat
import java.util.*

class CreateNotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notes)

        datetime.setText(SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(Date()))

        done.setOnClickListener{
            saveNote()
        }

        initMiscellaneou();

    }

    private fun initMiscellaneou() {

        val llBottomSheet = findViewById<LinearLayout>(R.id.layout_misc)
        val bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)

//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//
//        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {}
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
//        })

        layout_misc.setOnClickListener{
            if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
                else{
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        }
    }

    private fun saveNote() {
        val Title = title_edit_text.text.toString()
        val notes = note_text.text.toString()
        val subtitle = subtitle_edit_text.text.toString()
        if(Title.isEmpty()){
            view.snack("Title cannot be empty!")
            return
        }
        else if(notes.isEmpty()){
            view.snack("Notes Cannot be Empty")
            return
        }
        else{
            val note = Note()
            note.setTitle(Title)
            note.setSubtitle(subtitle)
            note.setNoteText(notes)
            note.setDateTime(datetime.text.toString())

            class SaveNotes: AsyncTask<Void, Void, Void>() {

                override fun doInBackground(vararg void: Void?): Void? {
                    NotesDatabase.getDatabase(applicationContext).noteDao().insertNote(note)
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    Log.d("Arptii", result.toString())

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent()
                        setResult(RESULT_OK,intent)
                        finish()
                    }, 500)

                }
            }

            SaveNotes().execute()
        }

    }

    fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }




}