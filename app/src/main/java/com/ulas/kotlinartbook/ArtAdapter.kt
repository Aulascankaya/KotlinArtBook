package com.ulas.kotlinartbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ulas.kotlinartbook.databinding.RecyclerRowBinding

class ArtAdapter(val artList : ArrayList<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {
    class  ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){
      // ArtHolder class holds each item in the RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {// Method called when a new ViewHolder is created
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)// Creating a new ViewHolder object and returning it
        return ArtHolder(binding)
    }

    override fun getItemCount(): Int {// Method that returns the number of items in the dataset
        return artList.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {// Method called when data is bound to a ViewHolder
        holder.binding.recyclerViewTextView.text = artList.get(position).name// Setting the artwork name to the ViewHolder's item
        holder.itemView.setOnClickListener {// Actions to be performed when the item is clicked
            val intent = Intent(holder.itemView.context,ArtActivity::class.java)
            intent.putExtra("info","old") //We create keywords to measure old or new sent.We will decide which page to go to in Artactivity.
            intent.putExtra("id",artList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }
}