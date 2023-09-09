package com.lrm.todolist.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.lrm.todolist.R
import com.lrm.todolist.ToDoApplication
import com.lrm.todolist.databinding.FragmentAddToDoBinding
import com.lrm.todolist.viewmodel.ToDoViewModel
import com.lrm.todolist.viewmodel.ToDoViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class AddToDoFragment : DialogFragment() {

    private var _binding: FragmentAddToDoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ToDoViewModel by activityViewModels {
        ToDoViewModelFactory(
            (activity?.application as ToDoApplication).database.toDoDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.custom_dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddToDoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBtn.setOnClickListener { dismiss() }

        binding.title.requestFocus()

        binding.date.setOnClickListener { showDatePickerDialog() }

        binding.time.setOnClickListener { showTimePickerDialog() }
    }

    private fun showTimePickerDialog() {

    }

    private fun showDatePickerDialog() {
        val calConstraints = CalendarConstraints.Builder()
            // Makes only dates from today forward selectable.
            .setValidator(DateValidatorPointForward.now())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(calConstraints.build())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build()

        datePicker.show(childFragmentManager, "Date Picker")

        datePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = sdf.format(datePicker.selection)
            binding.date.setText(date)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}