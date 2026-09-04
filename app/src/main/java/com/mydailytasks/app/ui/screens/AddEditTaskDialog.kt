package com.mydailytasks.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mydailytasks.app.data.model.RepeatType
import com.mydailytasks.app.data.model.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit
) {
    val today = remember { LocalDate.now() }
    val oneYearLater = remember { today.plusYears(1) }
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    var name by remember { mutableStateOf(taskToEdit?.name ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var startDate by remember { mutableStateOf(taskToEdit?.startDate ?: today.format(formatter)) }
    var endDate by remember { mutableStateOf(taskToEdit?.endDate ?: oneYearLater.format(formatter)) }
    var reminderTime by remember { mutableStateOf(taskToEdit?.reminderTime ?: "09:00") }
    var repeatType by remember { mutableStateOf(taskToEdit?.repeatType ?: RepeatType.DAILY) }
    var selectedDays by remember { mutableStateOf(taskToEdit?.selectedDays ?: listOf(1, 3, 5)) }
    var dayOfMonth by remember { mutableStateOf(taskToEdit?.dayOfMonth ?: 1) }

    val daysOfWeek = listOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (taskToEdit == null) "New Daily Task" else "Edit Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Task Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Optional Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dates
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder Time
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Reminder Time (HH:mm)") },
                    placeholder = { Text("08:30") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Repeat Type Options
                Text(
                    text = "Repeat Schedule",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val repeatOptions = listOf(
                    RepeatType.ONCE to "Once",
                    RepeatType.DAILY to "Daily",
                    RepeatType.WEEKLY to "Weekly",
                    RepeatType.MONTHLY to "Monthly",
                    RepeatType.CUSTOM to "Custom Days"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeatOptions.chunked(3).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { (type, label) ->
                                FilterChip(
                                    selected = repeatType == type,
                                    onClick = { repeatType = type },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                // Repeat Customization UI
                when (repeatType) {
                    RepeatType.WEEKLY -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Repeat on day:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            daysOfWeek.forEach { (dayId, dayName) ->
                                val isSelected = selectedDays.contains(dayId)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDays = listOf(dayId) },
                                    label = { Text(dayName) }
                                )
                            }
                        }
                    }
                    RepeatType.CUSTOM -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Select active weekdays:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            daysOfWeek.forEach { (dayId, dayName) ->
                                val isSelected = selectedDays.contains(dayId)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedDays = if (isSelected) {
                                            selectedDays.filter { it != dayId }
                                        } else {
                                            (selectedDays + dayId).sorted()
                                        }
                                    },
                                    label = { Text(dayName) }
                                )
                            }
                        }
                    }
                    RepeatType.MONTHLY -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = dayOfMonth.toString(),
                            onValueChange = { dayOfMonth = it.toIntOrNull()?.coerceIn(1, 31) ?: 1 },
                            label = { Text("Day of the Month (1-31)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    else -> Unit
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSaveTask(
                                    Task(
                                        id = taskToEdit?.id ?: 0L,
                                        name = name.trim(),
                                        description = description.trim(),
                                        startDate = startDate,
                                        endDate = endDate,
                                        reminderTime = reminderTime,
                                        repeatType = repeatType,
                                        selectedDays = selectedDays,
                                        dayOfMonth = dayOfMonth
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}
