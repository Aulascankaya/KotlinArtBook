package com.ulas.kotlinartbook

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ulas.kotlinartbook.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent> //to start another activity while expecting a result.
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //to request sensitive permissions like Camera or Storage.
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database= this.openOrCreateDatabase("Arts",Context.MODE_PRIVATE,null)// Opening or creating a SQLite database named "Arts"

        registerLauncher()

        val intent = intent// Getting the Intent object that started this activity
        val info = intent.getStringExtra("info")// Extracting the string extra named "info" from the Intent
        if (info.equals("new")){ // Checking if the value of "info" is "new".if the value of "info" is equal to "new", indicating a new record is being added.If it matches, it clears the input fields and makes the add button visible.
            binding.artNameText.setText("")
            binding.artistNameText.setText("")
            binding.yearText.setText("")
            binding.button.visibility = View.VISIBLE
            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.selectimage)
            binding.imageView.setImageBitmap(selectedImageBackground)
        }else{// If the value of "info" is not "new", it means an existing record is being edited.
            // This block retrieves the integer extra named "id" from the Intent, and fetches the record data
            // corresponding to that ID from the database, and populates the respective fields.
              binding.button.visibility = View.INVISIBLE
            val selectedId= intent.getIntExtra("id",1)
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val artNameIx = cursor.getColumnIndex("artname")
            val artistNameIx = cursor.getColumnIndex("artistname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.artNameText.setText(cursor.getString(artNameIx))
                binding.artistNameText.setText(cursor.getString(artistNameIx))
                binding.yearText.setText(cursor.getString(yearIx))
                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()


        }



    }


    fun save(view : View){
        // Extracting text from EditText fields for art name, artist name, and year
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        if(selectedBitmap != null){// Checking if a bitmap is selected
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)// Compressing the bitmap into a smaller size

            val outputStream = ByteArrayOutputStream()  // This stream is used to write data into a byte array.
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream) //compressed in PNG format with a quality of 50%.The compressed data is written into the ByteArrayOutputStream
            val byteArray = outputStream.toByteArray() //to convert the data inside the ByteArrayOutputStream into a byte array

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR,year VARCHAR, image BLOB)") // Creating a table named "arts" if it doesn't exist already
                val sqlString = "INSERT INTO arts (artname,artistname,year,image) VALUES(?,?,?,?)"  // Creating an SQL statement to insert data into the "arts" table
                val statement = database.compileStatement(sqlString)
                // Binding values to the SQL statement parameters
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)
                statement.execute() // Executing the SQL statement

            }catch (e:Exception){
                e.printStackTrace()
            }
            // Navigating back to the MainActivity after saving the data
            val intent = Intent(this@ArtActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun makeSmallerBitmap(image : Bitmap,maximumSize : Int): Bitmap{ //SQLite wants smaller size
        // Function to resize a given image to smaller dimensions
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble() // Calculate the width-height ratio of the image
        if (bitmapRatio>1){//landscape
            width=maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else{//Portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)  // Resize the scaled image and return
    }

    fun selectImage(view : View){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            //Android 33+ -> READ_MEDİA_IMAGES
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){  // After adding the uses permission to the manifest.xml file, we check the permission is allowed here. != not allowed , else -> allowed

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){  //Should I show the logic of getting permission to the user?
                    //rationale
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }else{
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)    //Go to Gallery
                activityResultLauncher.launch(intentToGallery)

            }
        }else{
            //Android 32- -> READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){  // After adding the uses permission to the manifest.xml file, we check the permission is allowed here. != not allowed , else -> allowed

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){  //Should I show the logic of getting permission to the user?
                    //rationale
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }else{
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)    //Go to Gallery
                activityResultLauncher.launch(intentToGallery)

            }
        }



    }

    private fun registerLauncher(){
        // Registering an ActivityResultLauncher
        activityResultLauncher =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            // Callback function executed when a result is returned from the launched activity
            if(result.resultCode == RESULT_OK){ // Checking if the result is successful
                val intentFromResult = result.data // Extracting the intent data from the result
                if(intentFromResult != null){      // Checking if the intent data is not null
                    val imageData = intentFromResult.data  // Extracting image data from the intent
                    //binding.imageView.setImageURI(imageData)

                    if(imageData!=null){  // Processing the image data if it's not null
                        try {
                            if(Build.VERSION.SDK_INT >=28) {// Decoding the bitmap from the image data based on API level
                                // Using ImageDecoder for Android 28 and later
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{ // Using MediaStore for Android versions prior to 28
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        }catch (e : Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if(result){//permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{//permission denied
                Toast.makeText(this@ArtActivity,"Permission needed!",Toast.LENGTH_LONG).show()
            }
        }
    }

}