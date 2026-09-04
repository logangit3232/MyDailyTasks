package com.mydailytasks.app.data.repository

import com.mydailytasks.app.data.local.TaskDao
import com.mydailytasks.app.data.model.RepeatType
import com.mydailytasks.app.data.model.Task
import com.mydailytasks.app.data.model.TaskCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasksFlow()
    val allCompletions: Flow<List<TaskCompletion>> = taskDao.getAllCompletionsFlow()

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun setTaskCompleted(taskId: Long, date: String, completed: Boolean) {
        if (completed) {
            taskDao.insertCompletion(TaskCompletion(taskId = taskId, completionDate = date))
        } else {
            taskDao.deleteCompletion(taskId, date)
        }
    }

    /**
     * Determines if a task is scheduled on target date based on start/end date and repeat rules
     */
    fun isTaskScheduledOnDate(task: Task, date: LocalDate): Boolean {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val startDate = LocalDate.parse(task.startDate, formatter)
        val endDate = LocalDate.parse(task.endDate, formatter)

        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            return false
        }

        return when (task.repeatType) {
            RepeatType.ONCE -> date == startDate
            RepeatType.DAILY -> true
            RepeatType.WEEKLY -> {
                val dayOfWeekVal = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
                if (task.selectedDays.isNotEmpty()) {
                    task.selectedDays.contains(dayOfWeekVal)
                } else {
                    dayOfWeekVal == startDate.dayOfWeek.value
                }
            }
            RepeatType.MONTHLY -> {
                val targetDom = task.dayOfMonth ?: startDate.dayOfMonth
                val maxDayThisMonth = date.lengthOfMonth()
                val effectiveDom = minOf(targetDom, maxDayThisMonth)
                date.dayOfMonth == effectiveDom
            }
            RepeatType.CUSTOM -> {
                val dayOfWeekVal = date.dayOfWeek.value
                task.selectedDays.contains(dayOfWeekVal)
            }
        }
    }
}
