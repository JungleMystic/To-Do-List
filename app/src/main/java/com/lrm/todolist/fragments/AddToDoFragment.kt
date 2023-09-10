package com.lrm.todolist.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
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
import com.lrm.todolist.constants.TAG
import com.lrm.todolist.database.ToDoEntity
import com.lrm.todolist.databinding.FragmentAddToDoBinding
import com.lrm.todolist.utils.AlarmReceiver
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

    private val calendar = Calendar.getInstance()
    private val setCalendar: Calendar = Calendar.getInstance()

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

        Log.i(TAG, "AddToDoFragment: toDoId -> $toDoId")

        if (toDoId > 0) {
            // Setting the title of this dialog based on the toDoId
            binding.dialogTitle.text = getString(R.string.edit_todo)

            viewModel.retrieveEvent(toDoId).observe(this.viewLifecycleOwner) {todo ->
                Log.i(TAG, "Retrieved from Room DB -> $todo")
                bindData(todo)
            }
            binding.save.text = getString(R.string.update)


        } else {
            // Setting the title of this dialog based on the toDoId
            binding.dialogTitle.text = getString(R.string.add_todo)

            // To get the focus to Title Edit text
            binding.title.requestFocus()

            // To save the To Do item in database
            binding.save.setOnClickListener {
                hideSoftKeyboard()
                addNewToDo()
            }
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
            binding.save.setOnClickListener {
                hideSoftKeyboard()
                updateToDo()
            }
        }
    }

    private fun addNewToDo() {
        Log.i(TAG, "addNewToDO is called")

        // Getting the data from edit text fields and storing in the below variables.
        val title = binding.title.text.toString().trim()
        val date = binding.date.text.toString()
        val time = binding.time.text.toString()

        // Check if all the fields are not empty
        if (viewModel.isEntryValid(requireContext(), title, date, time)) {
            viewModel.addNewToDo(title, date, time)
            setAlarm()
            Log.i(TAG, "addNewToDo -> setting alarm -> " +
                    "${setCalendar.get(Calendar.DAY_OF_MONTH)}/${setCalendar.get(Calendar.MONTH)}/${setCalendar.get(Calendar.YEAR)}  ${setCalendar.get(Calendar.HOUR)}:${setCalendar.get(Calendar.MINUTE)}")
            dismiss()
            Toast.makeText(requireContext(), "Successfully added...", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm() {
        val date = binding.date.text.toString()
        val time = binding.time.text.toString()

        val alarmManager = requireActivity().getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireActivity(), AlarmReceiver::class.java)
        intent.putExtra("Title", binding.title.text.toString())
        intent.putExtra("Message", "Reminder: $date  $time")

        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, setCalendar.timeInMillis, pendingIntent)
    }

    private fun updateToDo() {
        Log.i(TAG, "updateToDo is called")

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
        Log.i(TAG, "showDatePickerDialog is called")

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
            Log.i(TAG, "showDatePickerDialog: Selected Date -> ${datePicker.headerText}")
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = sdf.format(datePicker.selection)
            val date = sdf.parse(formattedDate)
            setCalendar.time = date!!
            Log.i(TAG, "showDatePickerDialog: Parsed Date -> $date")
            Log.i(TAG, "showDatePickerDialog: Formatted Date -> $formattedDate")
            binding.date.setText(formattedDate)
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        Log.i(TAG, "showTimePickerDialog is called")

        // To get the current time and show in the time picker dialog
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
            setCalendar.set(Calendar.HOUR_OF_DAY, hour)
            setCalendar.set(Calendar.MINUTE, minute)
            Log.i(TAG, "showTimePickerDialog: Selected Time -> $hour:$minute")
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            val formattedTime = sdf.format(calendar.time).replace("am", "AM").replace("pm", "PM")
            Log.i(TAG, "showTimePickerDialog: Formatted Time -> $formattedTime")
            binding.time.setText(formattedTime)
        }
        timePicker.addOnNegativeButtonClickListener {
            binding.time.setText("")
        }
        timePicker.addOnCancelListener {
            binding.time.setText("")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart is called")
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    // To hide the on screen keyboard
    private fun hideSoftKeyboard() {
        Log.i(TAG, "hideSoftKeyboard is called")
        val inputManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    override fun dismiss() {
        super.dismiss()
        Log.i(TAG, "dismiss is called")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView is called")
        _binding = null
    }
}