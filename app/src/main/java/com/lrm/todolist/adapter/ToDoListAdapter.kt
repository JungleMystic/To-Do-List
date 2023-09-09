package com.lrm.todolist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lrm.todolist.database.ToDoEntity
import com.lrm.todolist.databinding.ToDoListItemBinding

class ToDoListAdapter(
    private val onItemClicked: (ToDoEntity) -> Unit
): ListAdapter<ToDoEntity, ToDoListAdapter.ToDoItemViewHolder>(DiffCallback) {

    inner class ToDoItemViewHolder(
        private val binding: ToDoListItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(toDo: ToDoEntity) {
            binding.title.text = toDo.title
            binding.date.text = toDo.date
            binding.time.text = toDo.time
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ToDoEntity>() {
        override fun areItemsTheSame(oldItem: ToDoEntity, newItem: ToDoEntity): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ToDoEntity, newItem: ToDoEntity): Boolean {
            return oldItem.title == newItem.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoItemViewHolder {
        return ToDoItemViewHolder(
            ToDoListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ToDoItemViewHolder, position: Int) {
        val toDo = getItem(position)
        holder.bind(toDo)
        holder.itemView.setOnClickListener { onItemClicked(toDo) }
    }
}