package com.mydailytasks.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val startDate: String, // Format: YYYY-MM-DD
    val endDate: String,   // Format: YYYY-MM-DD
    val reminderTime: String, // Format: HH:mm (e.g. 08:30)
    val repeatType: RepeatType,
    val selectedDays: List<Int> = emptyList(), // 1 = Mon ... 7 = Sun
    val dayOfMonth: Int? = null, // 1 to 31 for MONTHLY repeat
    val createdAt: Long = System.currentTimeMillis()
)
