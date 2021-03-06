@file:Suppress("DEPRECATION")

package com.arpit.notify.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arpit.notify.R
import com.arpit.notify.database.NotesDatabase
import com.arpit.notify.entities.Note
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.android.synthetic.main.activity_create_notes.*
import kotlinx.android.synthetic.main.layout_add_url.view.*
import kotlinx.android.synthetic.main.layout_add_url.view.editText
import kotlinx.android.synthetic.main.layout_delete.view.*
import kotlinx.android.synthetic.main.layout_miscellaneous.*
import kotlinx.android.synthetic.main.layout_miscellaneous.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class CreateNotesActivity : AppCompatActivity() {

    private lateinit var selectedNoteColor: String
    private lateinit var selectedImagePath: String

    private var viewOrOpenedNote: Note? = null

    private var dialogAddUrl: AlertDialog? = null

    private val ioScope = CoroutineScope(Dispatchers.IO)

    var flag: Int = 0
    var wasInitiallyEmpty: Boolean = false

    private val REQUEST_GALLERY_CODE = 1
    private val REQUEST_CODE_SELECT_IMAGE = 2

    var ide: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notes)

        datetime.text = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(Date())

        initMiscellaneou()

        selectedNoteColor = "#333333"
        selectedImagePath= ""

        setSubTitleIndicatorColor()

        if(intent.getBooleanExtra("isViewOrUpdate", false)){
            viewOrOpenedNote = intent.getSerializableExtra("note") as Note?
            ide = viewOrOpenedNote?.id?.toLong()
            setVieworUpdateNote(viewOrOpenedNote)
        }
        if(intent.getBooleanExtra("fromArchive", false)){
            archiveTag.visibility = View.VISIBLE
        }

        if(viewOrOpenedNote != null && viewOrOpenedNote!!.getColor() != null){
            if(viewOrOpenedNote!!.getColor().equals("#4CAF50")){
                five()
            }
            if(viewOrOpenedNote!!.getColor().equals("#3A52Fc")){
                four()
            }
            if(viewOrOpenedNote!!.getColor().equals("#FF4842")){
                three()
            }
            if(viewOrOpenedNote!!.getColor().equals("#FDBE3B")){
                two()
            }
            if(viewOrOpenedNote!!.getColor().equals("#333333")){
                one()
            }
        }

        delete_button_for_url.setOnClickListener{
            textwebUrl.setText("")
            url_link.visibility = View.GONE
            saveNote()

        }
        delete_button_for_image.setOnClickListener{
            selectedImagePath=""
            image_on_create.setImageBitmap(null)
            image_on_create.visibility = View.GONE
            delete_button_for_image.visibility = View.GONE
            saveNote()
        }

        if(viewOrOpenedNote != null){
            delete.visibility = View.VISIBLE
            delete.setOnClickListener{
                deleteDialog()
            }
        }

        goback.setOnClickListener {
            finish()
        }

        title_edit_text.addTextChangedListener((object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                ing progress_lottie.visibility = View.VISIBLE
//                progress_lottie.playAnimation()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
//                cancelTimer()
                saveNote()
            }

        }))

        subtitle_edit_text.addTextChangedListener((object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                cancelTimer()
            }

            override fun afterTextChanged(p0: Editable?) {
//                cancelTimer()
                saveNote()
            }

        }))

        note_text.addTextChangedListener((object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                cancelTimer()
            }

            override fun afterTextChanged(p0: Editable?) {
//                cancelTimer()
                saveNote()
            }

        }))
        val Title = title_edit_text.text.toString()
        val notes = note_text.text.toString()
        val subtitle = subtitle_edit_text.text.toString()
        if (Title.isEmpty() && notes.isEmpty() && subtitle.isEmpty() && url_link.visibility == View.GONE && selectedImagePath.isEmpty()){
            wasInitiallyEmpty = true
        }
    }

    private fun setVieworUpdateNote(noteAllready: Note?) {
        if (noteAllready != null) {
            title_edit_text.setText(noteAllready.getTitle())
            subtitle_edit_text.setText(noteAllready.getSubtitle())
            note_text.setText(noteAllready.getNoteText())
            datetime.setText(noteAllready.getDateTime())

            if(noteAllready.getImagePath() != null && !noteAllready.getImagePath().toString().trim().isEmpty()){
                image_on_create.setImageBitmap(BitmapFactory.decodeFile(noteAllready.getImagePath()))
                image_on_create.setVisibility(View.VISIBLE)
                delete_button_for_image.visibility = View.VISIBLE
                selectedImagePath = noteAllready.getImagePath()
            }
            if(noteAllready.getWebLink() != null && !noteAllready.getWebLink().toString().trim().isEmpty()){
                textwebUrl.setText(noteAllready.getWebLink())
                url_link.visibility = View.VISIBLE

            }
        }


    }
    private fun setSubTitleIndicatorColor() {
        val gradientDrawable:GradientDrawable  = view.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor))
    }

    private fun initMiscellaneou() {


        val llBottomSheet = findViewById<LinearLayout>(R.id.layout_misc)
        val bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)

        url.setOnClickListener{
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddUrlDialog()
        }

        layout_misc.setOnClickListener{
            if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
                else{
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        }

        tick_one.setOnClickListener{
            one()
        }
        tick_two.setOnClickListener{
           two()
        }
        tick_three.setOnClickListener{
           three()
        }
        tick_four.setOnClickListener{
            four()
        }
        tick_five.setOnClickListener{
           five()
        }



        layout_add_image.setOnClickListener{
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val permissio = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)

            if(permissio != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Permission to access the gallery is required for this app to add image notes")
                                    .setTitle("Permission required")

                                    builder.setPositiveButton("OK"
                                    ) { dialog, id ->
                                makeRequest()
                            }

                            val dialog = builder.create()
                    dialog.show()
                } else {
                    makeRequest()
                }
            }
            else {
                addImage()
            }
        }
    }

    private fun deleteDialog() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.layout_delete, null)
        val infoDialogBuilder = AlertDialog.Builder(this@CreateNotesActivity)
        infoDialogBuilder.setView(view)
        val infoDialog = infoDialogBuilder.create()
        infoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        infoDialog.setContentView(view)
        infoDialog.show()

        view.deleteYes.setOnClickListener {
            class DeleteNoteTask: AsyncTask<Void, Void, Void>() {

                override fun doInBackground(vararg void: Void?): Void? {
                    NotesDatabase.getDatabase(applicationContext).noteDao().delete(viewOrOpenedNote)
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent()
                        intent.putExtra("isNoteDeleted", true)
                        setResult(RESULT_OK, intent)
                        finish()
                    }, 300)
                }
            }
            DeleteNoteTask().execute()

        }
        view.deleteNo.setOnClickListener {
            infoDialog.dismiss()
        }
    }

    private fun five() {
        selectedNoteColor = "#4CAF50"

        tick_one.setImageResource(0)
        tick_two.setImageResource(0)
        tick_three.setImageResource(0)
        tick_four.setImageResource(0)
        tick_five.setImageResource(R.drawable.ic_single_done)
        setSubTitleIndicatorColor()
        saveNote()
    }
    private fun four() {
        selectedNoteColor = "#3A52Fc"

        tick_one.setImageResource(0)
        tick_two.setImageResource(0)
        tick_three.setImageResource(0)
        tick_four.setImageResource(R.drawable.ic_single_done)
        tick_five.setImageResource(0)
        setSubTitleIndicatorColor()
        saveNote()
    }
    private fun three() {
        selectedNoteColor = "#FF4842"

        tick_one.setImageResource(0)
        tick_two.setImageResource(0)
        tick_three.setImageResource(R.drawable.ic_single_done)
        tick_four.setImageResource(0)
        tick_five.setImageResource(0)
        setSubTitleIndicatorColor()
        saveNote()
    }
    private fun two() {
        selectedNoteColor = "#FDBE3B"

        tick_one.setImageResource(0)
        tick_two.setImageResource(R.drawable.ic_single_done)
        tick_three.setImageResource(0)
        tick_four.setImageResource(0)
        tick_five.setImageResource(0)
        setSubTitleIndicatorColor()
        saveNote()
    }
    private fun one() {
        selectedNoteColor = "#333333"

        tick_one.setImageResource(R.drawable.ic_single_done)
        tick_two.setImageResource(0)
        tick_three.setImageResource(0)
        tick_four.setImageResource(0)
        tick_five.setImageResource(0)
        setSubTitleIndicatorColor()
        saveNote()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_GALLERY_CODE  && grantResults.size > 0)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                addImage()
            }else{
                Toast.makeText(applicationContext, "Permission Denied!", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun addImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if(intent.resolveActivity(packageManager)!=null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_GALLERY_CODE)
    }

    private fun saveNote() {
        Log.d("sxxs", "SaveNotes called")
        flag = 1
                val database = NotesDatabase.getDatabase(applicationContext)
                val Title = title_edit_text.text.toString()
                val notes = note_text.text.toString()
                val subtitle = subtitle_edit_text.text.toString()

                if (Title.isEmpty() && notes.isEmpty() && subtitle.isEmpty() && selectedImagePath.isEmpty() && url_link.visibility == View.GONE) {
                    return
                }
                val note = Note()
                note.setTitle(Title)
                note.setSubtitle(subtitle)
                note.setNoteText(notes)
                note.setDateTime(datetime.text.toString())
                note.setColor(selectedNoteColor)
                note.setImagePath(selectedImagePath)

                if (url_link.visibility == View.VISIBLE && !textwebUrl.text.toString().isEmpty()) {
                    note.setWebLink(textwebUrl.text.toString())
                }
                if (intent.getBooleanExtra("fromArchive", false)) {
                    note.setArch(true)
                }
                if (ide != null) {
                    note.setId(ide!!.toInt())
                }

                    class SaveNotes : AsyncTask<Void, Void, Void>() {
                        override fun doInBackground(vararg void: Void?): Void? {
                            ide = database.noteDao().insertNote(note)
                            return null
                        }
                    }
                    SaveNotes().execute()
    }

    private fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                val selectImageUri: Uri? = data.data
                if(selectImageUri!=null){
                    try {

                        val name: String = getFileName(selectImageUri)
                        val filepath = getPathFromUri(selectImageUri)

                        ioScope.launch {
                            selectedImagePath = getExternalFilesDir("Feather Notes")!!.absolutePath + File.separator + name
                            val destinationfile = File(selectedImagePath)

                            var compressedImageFile = Compressor.compress(this@CreateNotesActivity, File(filepath)) {
                                default()
                                destination(destinationfile)
                            }
                            val finalBitmap = BitmapFactory.decodeFile(compressedImageFile.path)

                            runOnUiThread {
                                image_on_create.setImageBitmap(finalBitmap)
                                image_on_create.visibility = View.VISIBLE
                                delete_button_for_image.visibility = View.VISIBLE
                                saveNote()
                            }
                        }
                    }
                    catch (e: Exception){
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri): String {
        val filePath: String
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        if(cursor == null){
            filePath = contentUri.path.toString()
        }
        else{
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath= cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    private fun showAddUrlDialog(){
        if (dialogAddUrl == null) {
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.layout_add_url, null)
            val infoDialogBuilder = AlertDialog.Builder(this@CreateNotesActivity)
            infoDialogBuilder.setView(view)
            val infoDialog = infoDialogBuilder.create()
            infoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            infoDialog.setContentView(view)
            infoDialog.show()
            view.addYes.setOnClickListener {
                url_link.visibility = View.VISIBLE
                textwebUrl.setText(view.editText.text.toString())
                saveNote()
                infoDialog.dismiss()
            }
            view.cancelNo.setOnClickListener {
                infoDialog.dismiss()
            }
        }
    }

    override fun onBackPressed() {

        val Title = title_edit_text.text.toString()
        val notes = note_text.text.toString()
        val subtitle = subtitle_edit_text.text.toString()

        if (Title.isEmpty() && notes.isEmpty() && subtitle.isEmpty() && url_link.visibility == View.GONE  && selectedImagePath.isEmpty()) {
            if(flag == 1) {
                class DeleteNoteTask : AsyncTask<Void, Void, Void>() {

                    override fun doInBackground(vararg void: Void?): Void? {
                        val note = Note()
                        note.setId(ide!!.toInt())
                        NotesDatabase.getDatabase(applicationContext).noteDao().delete(note)
                        return null
                    }

                    override fun onPostExecute(result: Void?) {
                        super.onPostExecute(result)
                        if (wasInitiallyEmpty) {
                            finish()
                        }
                        if (!wasInitiallyEmpty) {
                            val intent = Intent()
                            intent.putExtra("isNoteDeleted", true)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                }
                DeleteNoteTask().execute()
            }
            if(flag == 0){
                finish()
            }
        }
        else {
            Log.d("sxxs", "back from else pressed")
            val intent = Intent()
            intent.putExtra("isNoteDeleted", false)
            intent.putExtra("FlagReceived", flag)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    @SuppressLint("Recycle")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }  //gets the file name of the selected image
}