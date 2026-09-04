package com.mydailytasks.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "task_completions",
    primaryKeys = ["taskId", "completionDate"],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"]), Index(value = ["completionDate"])]
)
data class TaskCompletion(
    val taskId: Long,
    val completionDate: String, // Format: YYYY-MM-DD
    val completedAt: Long = System.currentTimeMillis()
)
