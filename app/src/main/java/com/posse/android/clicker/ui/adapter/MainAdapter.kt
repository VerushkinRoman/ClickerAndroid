package com.posse.android.clicker.ui.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter : RecyclerView.Adapter<MainAdapter.RecyclerItemViewHolder>() {

    private val data: MutableList<String> = mutableListOf()

    fun add(data: String) {
        this.data.add(data)
        notifyItemInserted(this.data.size)
        if (this.data.size > 50) {
            this.data.removeAt(0)
            notifyItemRemoved(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerItemViewHolder {
        return RecyclerItemViewHolder(
            TextView(parent.context)
        )
    }

    override fun onBindViewHolder(holder: RecyclerItemViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class RecyclerItemViewHolder(private val textView: TextView) :
        RecyclerView.ViewHolder(textView) {

        fun bind(data: String) {
            textView.text = data
        }
    }
}