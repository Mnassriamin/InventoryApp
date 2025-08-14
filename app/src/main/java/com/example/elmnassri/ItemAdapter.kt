package com.example.elmnassri

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

// The adapter now takes a function as a parameter
class ItemAdapter(private val onItemClicked: (Item) -> Unit) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        // Set the click listener for the whole card
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameView: TextView = itemView.findViewById(R.id.item_name)
        private val itemPriceView: TextView = itemView.findViewById(R.id.item_price)
        private val itemImageView: ImageView = itemView.findViewById(R.id.item_image)

        fun bind(item: Item) {
            itemNameView.text = item.name
            itemPriceView.text = "${String.format("%.2f", item.price)} TND"
            itemImageView.load(item.imageUri) {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_foreground)
            }
        }

        companion object {
            fun create(parent: ViewGroup): ItemViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item, parent, false)
                return ItemViewHolder(view)
            }
        }
    }

    class ItemsComparator : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}