package com.lrm.todolist.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lrm.todolist.R
import com.lrm.todolist.database.ToDoEntity
import com.lrm.todolist.databinding.ToDoListItemBinding
import com.lrm.todolist.viewmodel.ToDoViewModel

class ToDoListAdapter(
    private val activity: Activity,
    private val context: Context,
    private val viewModel: ToDoViewModel,
    private val onItemClicked: (ToDoEntity) -> Unit
): ListAdapter<ToDoEntity, ToDoListAdapter.ToDoItemViewHolder>(DiffCallback) {

    inner class ToDoItemViewHolder(
        private val binding: ToDoListItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(toDo: ToDoEntity) {
            binding.title.text = toDo.title
            binding.date.text = toDo.date
            binding.time.text = toDo.time

            if (toDo.isCompleted == 1) {
                binding.doneCheckbox.isChecked = true
                binding.toDoCard.cardElevation = 0.0f
                binding.toDoCard.strokeColor = ContextCompat.getColor(context, R.color.light_grey)
                binding.toDoCard.strokeWidth = 1
                binding.title.setTypeface(null, Typeface.NORMAL)
                binding.title.setTextColor(ContextCompat.getColor(context, R.color.light_grey))
                binding.readIcon.visibility = View.VISIBLE
            } else {
                binding.doneCheckbox.isChecked = false
                binding.toDoCard.cardElevation = 7.0f
                binding.toDoCard.strokeColor = ContextCompat.getColor(context, R.color.light_grey)
                binding.toDoCard.strokeWidth = 0.5.toInt()
                binding.title.setTypeface(null, Typeface.BOLD)
                binding.title.setTextColor(ContextCompat.getColor(context, R.color.blue))
                binding.readIcon.visibility = View.GONE
            }

            binding.doneCheckbox.setOnClickListener {
                if (binding.doneCheckbox.isChecked) {
                    viewModel.updateCompletedToDo(toDo.id, 1)
                } else {
                    viewModel.updateCompletedToDo(toDo.id, 0)
                }
            }
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
        holder.itemView.setOnLongClickListener {
            showDeleteDialog(toDo)
            true
        }
    }

    private fun showDeleteDialog(toDo: ToDoEntity) {
        val dialogView = activity.layoutInflater.inflate(R.layout.custom_delete_dialog, null)
        val yesTv = dialogView.findViewById<TextView>(R.id.yes_tv)
        val noTv = dialogView.findViewById<TextView>(R.id.no_tv)

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder.setCancelable(true)

        val deleteDialog = builder.create()
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteDialog.show()

        yesTv.setOnClickListener {
            viewModel.deleteEvent(toDo)
            deleteDialog.dismiss()
        }

        noTv.setOnClickListener {
            deleteDialog.dismiss()
        }
    }
}