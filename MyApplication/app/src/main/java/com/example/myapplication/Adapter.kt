package com.example.myapplication


import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class Adapter  (
    private val list: List<GarbageModel>
) :
    RecyclerView.Adapter<Adapter.MyView>() {
    // View Holder class which
    // extends RecyclerView.ViewHolder
    inner class MyView(view: View) : RecyclerView.ViewHolder(view) {
        val image=view.findViewById<AppCompatImageView>(R.id.img)
        val uname=view.findViewById<TextView>(R.id.name)

        val add=view.findViewById<TextView>(R.id.address)

        val btnMap=view.findViewById<Button>(R.id.button1)

    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyView {

        // Inflate item.xml using LayoutInflator
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.admin,
                parent,
                false
            )

        // return itemView
        return MyView(itemView)
    }

    override fun onBindViewHolder(
        holder: MyView,
        position: Int
    ) {
        val currentItem = list[position]
        holder.add.text=currentItem.address
        holder.uname.text=currentItem.uName
        Glide.with(holder.image.context)
            .load(currentItem.imageUrl)
            .into(holder.image)

        holder.btnMap.setOnClickListener {
            val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${currentItem.lat},${currentItem.log}")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = mapUri
            it.context.startActivity(intent)
        }
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    override fun getItemCount(): Int {
        return list.size
    }
}
