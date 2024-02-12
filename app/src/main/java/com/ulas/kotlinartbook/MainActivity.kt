package com.ulas.kotlinartbook

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.ulas.kotlinartbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var artList : ArrayList<Art>
    private lateinit var artAdapter : ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        artList= ArrayList<Art>()

        artAdapter= ArtAdapter(artList)// Initializing ArtAdapter with artList
        binding.recyclerView.layoutManager=LinearLayoutManager(this)// Configuring RecyclerView with LinearLayoutManager and setting artAdapter as its adapter
        binding.recyclerView.adapter = artAdapter

        try {
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null) // Opening a SQLite database named "Arts"
            val cursor = database.rawQuery("SELECT * FROM arts",null) // Executing a raw query to select all records from the "arts" table
            val artNameIx = cursor.getColumnIndex("artname")   // Getting the column index of the "artname" and "id" columns in the result set
            val idIx = cursor.getColumnIndex("id")
            while (cursor.moveToNext()){// Iterating through the result set using a while loop
                val name = cursor.getString(artNameIx)// Getting the values of "artname" and "id" columns for the current row
                val id = cursor.getInt(idIx)
                val art = Art(name, id)// Creating an Art object with the retrieved values and adding it to the list
                artList.add(art)
            }
            artAdapter.notifyDataSetChanged()

            cursor.close()// Closing the cursor after iterating through the result set
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {   //Define the menu
        //inflater

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {  // What will happen when click?

        if(item.itemId == R.id.add_art_item){
            val intent = Intent(this@MainActivity,ArtActivity::class.java)    // Main to Art
            intent.putExtra("info","new") //We create keywords to measure old or new sent. We will decide which page to go to in Artactivity.
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

}