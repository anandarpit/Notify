package com.arpit.notify.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
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

    lateinit var selectedNoteColor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notes)

        datetime.setText(SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(Date()))

        done.setOnClickListener{
            saveNote()
        }

        initMiscellaneou();

        selectedNoteColor = "#333333"

        setSubTitleIndicatorColor()

    }

    private fun setSubTitleIndicatorColor() {
        var gradientDrawable:GradientDrawable  = view.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor))
    }

    private fun initMiscellaneou() {

        val llBottomSheet = findViewById<LinearLayout>(R.id.layout_misc)
        val bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)

        layout_misc.setOnClickListener{
            if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
                else{
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        }

        tick_one.setOnClickListener{
            selectedNoteColor = "#333333"

            tick_one.setImageResource(R.drawable.ic_single_done)
            tick_two.setImageResource(0)
            tick_three.setImageResource(0)
            tick_four.setImageResource(0)
            tick_five.setImageResource(0)
            setSubTitleIndicatorColor()
        }

        tick_two.setOnClickListener{
            selectedNoteColor = "#FDBE3B"

            tick_one.setImageResource(0)
            tick_two.setImageResource(R.drawable.ic_single_done)
            tick_three.setImageResource(0)
            tick_four.setImageResource(0)
            tick_five.setImageResource(0)
            setSubTitleIndicatorColor()
        }
        tick_three.setOnClickListener{
            selectedNoteColor = "#FF4842"

            tick_one.setImageResource(0)
            tick_two.setImageResource(0)
            tick_three.setImageResource(R.drawable.ic_single_done)
            tick_four.setImageResource(0)
            tick_five.setImageResource(0)
            setSubTitleIndicatorColor()
        }
        tick_four.setOnClickListener{
            selectedNoteColor = "#3A52Fc"

            tick_one.setImageResource(0)
            tick_two.setImageResource(0)
            tick_three.setImageResource(0)
            tick_four.setImageResource(R.drawable.ic_single_done)
            tick_five.setImageResource(0)
            setSubTitleIndicatorColor()
        }
        tick_five.setOnClickListener{
            selectedNoteColor = "#4CAF50"

            tick_one.setImageResource(0)
            tick_two.setImageResource(0)
            tick_three.setImageResource(0)
            tick_four.setImageResource(0)
            tick_five.setImageResource(R.drawable.ic_single_done)
            setSubTitleIndicatorColor()
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
            note.setColor(selectedNoteColor)

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