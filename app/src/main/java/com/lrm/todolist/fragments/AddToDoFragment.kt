package com.lrm.todolist.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.lrm.todolist.R
import com.lrm.todolist.ToDoApplication
import com.lrm.todolist.database.ToDoEntity
import com.lrm.todolist.databinding.FragmentAddToDoBinding
import com.lrm.todolist.viewmodel.ToDoViewModel
import com.lrm.todolist.viewmodel.ToDoViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddToDoFragment(private val toDoId: Int = -1) : DialogFragment() {

    private var _binding: FragmentAddToDoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ToDoViewModel by activityViewModels {
        ToDoViewModelFactory(
            (activity?.application as ToDoApplication).database.toDoDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.custom_dialog)
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
        // To dismiss the dialog fragment
        binding.closeBtn.setOnClickListener { dismiss() }

        if (toDoId > 0) {
            // Setting the title of this dialog based on the toDoId
            binding.dialogTitle.text = getString(R.string.edit_todo)

            viewModel.retrieveEvent(toDoId).observe(this.viewLifecycleOwner) {todo ->
                bindData(todo)
            }
            binding.save.text = getString(R.string.update)


        } else {
            // Setting the title of this dialog based on the toDoId
            binding.dialogTitle.text = getString(R.string.add_todo)

            // To get the focus to Title Edit text
            binding.title.requestFocus()

            // To save the To Do item in database
            binding.save.setOnClickListener { addNewToDO() }
        }

        // To show the date picker dialog
        binding.date.setOnClickListener { showDatePickerDialog() }

        // To show the time picker dialog
        binding.time.setOnClickListener { showTimePickerDialog() }

        // To dismiss the dialog fragment
        binding.cancel.setOnClickListener { dismiss() }
    }

    private fun bindData(todo: ToDoEntity) {
        binding.apply {
            title.setText(todo.title, TextView.BufferType.SPANNABLE)
            date.setText(todo.date, TextView.BufferType.SPANNABLE)
            time.setText(todo.time, TextView.BufferType.SPANNABLE)

            // To update the To Do item in database
            binding.save.setOnClickListener {updateToDo()}
        }
    }

    private fun addNewToDO() {
        // Getting the data from edit text fields and storing in the below variables.
        val title = binding.title.text.toString().trim()
        val date = binding.date.text.toString()
        val time = binding.time.text.toString()

        // Check if all the fields are not empty
        if (viewModel.isEntryValid(requireContext(), title, date, time)) {
            viewModel.addNewToDo(title, date, time)
            dismiss()
            Toast.makeText(requireContext(), "Successfully added...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToDo() {
        // Getting the data from edit text fields and storing in the below variables.
        val title = binding.title.text.toString().trim()
        val date = binding.date.text.toString()
        val time = binding.time.text.toString()

        // Check if all the fields are not empty
        if (viewModel.isEntryValid(requireContext(), title, date, time)) {
            viewModel.getUpdatedToDO(toDoId, title, date, time, 0)
            dismiss()
            Toast.makeText(requireContext(), "Successfully updated...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog() {
        // Makes only dates from today forward selectable.
        val dateConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        // building the date picker dialog with additional set methods
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(dateConstraints.build())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
                .build()

        datePicker.show(childFragmentManager, "Date Picker")

        datePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = sdf.format(datePicker.selection)
            binding.date.setText(date)
        }
    }

    private fun showTimePickerDialog() {
        // To get the current time and show in the time picker dialog
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setTitleText("Set the Time")
                .setInputMode(INPUT_MODE_CLOCK)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTheme(R.style.ThemeOverlay_App_MaterialTime)
                .build()

        timePicker.show(childFragmentManager, "Time Picker")

        timePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val hour = timePicker.hour
            val minute = timePicker.minute
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            binding.time.setText(sdf.format(calendar.time).replace("am", "AM").replace("pm", "PM"))
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