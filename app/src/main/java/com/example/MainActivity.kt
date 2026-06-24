package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.ClockScreen
import com.example.ui.TimerScreen
import com.example.ui.StopwatchScreen
import com.example.ui.CalendarScreen
import com.example.ui.AiHelperScreen
import com.example.ui.CreditDialog
import com.example.ui.SettingsDialog
import com.example.data.SettingsManager
import androidx.compose.material.icons.filled.Settings

enum class AppTab(val title: String, val icon: ImageVector, val testTag: String) {
    CLOCK("Clock", Icons.Default.Schedule, "tab_clock"),
    TIMER("Timer", Icons.Default.Refresh, "tab_timer"),
    STOPWATCH("Stopwatch", Icons.Default.PlayArrow, "tab_stopwatch"),
    CALENDAR("Calendar", Icons.Default.CalendarToday, "tab_calendar"),
    AI_HELPER("AI Helper", Icons.Default.Favorite, "tab_ai_helper")
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsManager.init(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(AppTab.CLOCK) }
                var showCreditsDialog by remember { mutableStateOf(false) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Samay: " + currentTab.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { showSettingsDialog = true },
                                    modifier = Modifier.testTag("toolbar_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { showCreditsDialog = true },
                                    modifier = Modifier.testTag("toolbar_credits_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Creative Author Credits",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = NavigationBarDefaults.Elevation
                        ) {
                            AppTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (currentTab == tab) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    
                    when (currentTab) {
                        AppTab.CLOCK -> {
                            ClockScreen(
                                onNavigateToCalendar = { currentTab = AppTab.CALENDAR },
                                modifier = screenModifier
                            )
                        }
                        AppTab.TIMER -> {
                            TimerScreen(modifier = screenModifier)
                        }
                        AppTab.STOPWATCH -> {
                            StopwatchScreen(modifier = screenModifier)
                        }
                        AppTab.CALENDAR -> {
                            CalendarScreen(modifier = screenModifier)
                        }
                        AppTab.AI_HELPER -> {
                            AiHelperScreen(modifier = screenModifier)
                        }
                    }
                }

                // Show Abhishek Singh and AI Creative Credits
                if (showCreditsDialog) {
                    CreditDialog(
                        onDismiss = { showCreditsDialog = false }
                    )
                }

                // Show Settings Dialog
                if (showSettingsDialog) {
                    SettingsDialog(
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }
    }
}
