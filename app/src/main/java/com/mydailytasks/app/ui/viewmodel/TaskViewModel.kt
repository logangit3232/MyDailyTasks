package com.mydailytasks.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydailytasks.app.data.local.AppDatabase
import com.mydailytasks.app.data.model.Task
import com.mydailytasks.app.data.model.TaskCompletion
import com.mydailytasks.app.data.repository.TaskRepository
import com.mydailytasks.app.util.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TaskWithStatus(
    val task: Task,
    val isCompleted: Boolean
)

data class ReportsData(
    val completedToday: Int = 0,
    val completedThisWeek: Int = 0,
    val completedThisMonth: Int = 0,
    val totalCompleted: Int = 0,
    val totalPending: Int = 0,
    val completionPercentage: Float = 0f
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    val selectedDate = MutableStateFlow(LocalDate.now())
    val allTasks: StateFlow<List<Task>>
    val allCompletions: StateFlow<List<TaskCompletion>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = TaskRepository(db.taskDao())

        allTasks = repository.allTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allCompletions = repository.allCompletions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    // Tasks scheduled for the currently selected date
    val tasksForSelectedDate: StateFlow<List<TaskWithStatus>> = combine(
        selectedDate,
        allTasks,
        allCompletions
    ) { date, tasks, completions ->
        val dateString = date.format(dateFormatter)
        val completedTaskIds = completions
            .filter { it.completionDate == dateString }
            .map { it.taskId }
            .toSet()

        tasks.filter { repository.isTaskScheduledOnDate(it, date) }
            .map { task ->
                TaskWithStatus(
                    task = task,
                    isCompleted = completedTaskIds.contains(task.id)
                )
            }
            .sortedWith(compareBy({ it.isCompleted }, { it.task.reminderTime }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports calculation
    val reportsData: StateFlow<ReportsData> = combine(
        allTasks,
        allCompletions
    ) { tasks, completions ->
        val today = LocalDate.now()
        val todayStr = today.format(dateFormatter)

        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val endOfWeek = startOfWeek.plusDays(6)

        val completedToday = completions.count { it.completionDate == todayStr }

        val completedThisWeek = completions.count {
            runCatching {
                val d = LocalDate.parse(it.completionDate, dateFormatter)
                !d.isBefore(startOfWeek) && !d.isAfter(endOfWeek)
            }.getOrDefault(false)
        }

        val completedThisMonth = completions.count {
            runCatching {
                val d = LocalDate.parse(it.completionDate, dateFormatter)
                d.year == today.year && d.month == today.month
            }.getOrDefault(false)
        }

        val totalCompleted = completions.size
        // Scheduled today pending
        val todayScheduled = tasks.count { repository.isTaskScheduledOnDate(it, today) }
        val pendingToday = maxOf(0, todayScheduled - completedToday)

        val percentage = if (todayScheduled > 0) {
            (completedToday.toFloat() / todayScheduled) * 100f
        } else {
            0f
        }

        ReportsData(
            completedToday = completedToday,
            completedThisWeek = completedThisWeek,
            completedThisMonth = completedThisMonth,
            totalCompleted = totalCompleted,
            totalPending = pendingToday,
            completionPercentage = percentage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsData())

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun toggleTaskCompletion(task: Task, date: LocalDate, currentStatus: Boolean) {
        viewModelScope.launch {
            val dateString = date.format(dateFormatter)
            repository.setTaskCompleted(task.id, dateString, !currentStatus)
        }
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            val insertedId = if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
                task.id
            }
            val finalTask = task.copy(id = insertedId)
            AlarmScheduler.scheduleReminder(getApplication(), finalTask)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(getApplication(), task.id)
            repository.deleteTask(task)
        }
    }
}
