package com.lrm.todolist.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "to_do_table")
data class ToDoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    val title: String = "",
    @ColumnInfo(name = "date")
    val date: String = "",
    @ColumnInfo(name = "time")
    val time: String = "",
    @ColumnInfo(name = "isCompleted")
    val isCompleted: Int = 0
)
