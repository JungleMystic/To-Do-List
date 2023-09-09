package com.lrm.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.lrm.todolist.database.ToDoDao
import com.lrm.todolist.database.ToDoEntity
import kotlinx.coroutines.launch

class ToDoViewModel(
    private val toDoDao: ToDoDao
): ViewModel() {

    val getAll = toDoDao.getAll().asLiveData()

    // To insert a To do item in the database
    private fun insertToDo(todo: ToDoEntity) {
        viewModelScope.launch {
            toDoDao.insert(todo)
        }
    }

    // Get the New To do item and calling insertTodo function
    fun addNewToDo(title: String, date: String, time: String) {
        val toDo = ToDoEntity(title = title, date = date, time = time)
        insertToDo(toDo)
    }
}

class ToDoViewModelFactory(private val toDoDao: ToDoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToDoViewModel(toDoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}