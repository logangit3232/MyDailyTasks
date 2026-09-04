package com.mydailytasks.app.data.local

import androidx.room.*
import com.mydailytasks.app.data.model.Task
import com.mydailytasks.app.data.model.TaskCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks ORDER BY reminderTime ASC")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    // --- Task Completions History ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: TaskCompletion)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId AND completionDate = :date")
    suspend fun deleteCompletion(taskId: Long, date: String)

    @Query("SELECT * FROM task_completions WHERE completionDate = :date")
    fun getCompletionsForDate(date: String): Flow<List<TaskCompletion>>

    @Query("SELECT * FROM task_completions")
    fun getAllCompletionsFlow(): Flow<List<TaskCompletion>>

    @Query("SELECT COUNT(*) FROM task_completions WHERE completionDate = :date")
    suspend fun getCompletedCountForDate(date: String): Int
}
