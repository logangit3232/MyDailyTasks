package com.mydailytasks.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mydailytasks.app.data.model.Task
import com.mydailytasks.app.ui.screens.AddEditTaskDialog
import com.mydailytasks.app.ui.screens.CalendarScreen
import com.mydailytasks.app.ui.screens.HomeScreen
import com.mydailytasks.app.ui.screens.ReportsScreen
import com.mydailytasks.app.ui.theme.MyDailyTasksTheme
import com.mydailytasks.app.ui.viewmodel.TaskViewModel
import com.mydailytasks.app.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Notification permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Notification Channel
        NotificationHelper.createNotificationChannel(this)

        // 2. Request Notification Permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyDailyTasksTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    var showTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = "Today") },
                    label = { Text("Today") }
                )
                NavigationBarItem(
                    selected = currentRoute == "calendar",
                    onClick = { navController.navigate("calendar") },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = currentRoute == "reports",
                    onClick = { navController.navigate("reports") },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onAddTaskClick = {
                        taskToEdit = null
                        showTaskDialog = true
                    },
                    onEditTaskClick = { task ->
                        taskToEdit = task
                        showTaskDialog = true
                    }
                )
            }
            composable("calendar") {
                CalendarScreen(viewModel = viewModel)
            }
            composable("reports") {
                ReportsScreen(viewModel = viewModel)
            }
        }

        if (showTaskDialog) {
            AddEditTaskDialog(
                taskToEdit = taskToEdit,
                onDismiss = { showTaskDialog = false },
                onSaveTask = { task ->
                    viewModel.saveTask(task)
                    showTaskDialog = false
                }
            )
        }
    }
}
