package com.lrm.todolist.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(todo: ToDoEntity)

    @Update
    suspend fun update(todo: ToDoEntity)

    @Delete
    suspend fun delete(todo: ToDoEntity)

    @Query("SELECT * FROM to_do_table ORDER BY id DESC")
    fun getAll(): Flow<List<ToDoEntity>>

    @Query("SELECT * FROM to_do_table WHERE id = :id")
    fun getToDo(id: Int): Flow<ToDoEntity>

    @Query("UPDATE to_do_table SET isShow = :isShow WHERE id LIKE :id")
    fun updateIsShown(id: Int, isShow: Int)
}