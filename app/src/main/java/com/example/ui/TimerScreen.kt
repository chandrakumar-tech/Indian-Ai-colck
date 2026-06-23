package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class TimerPreset(
    val title: String,
    val durationSeconds: Long,
    val description: String
)

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        TimerPreset("Masala Chai", 180L, "Brew perfect Indian tea"),
        TimerPreset("Pranayama", 600L, "Controlled deep yoga breathing"),
        TimerPreset("Pomodoro", 1500L, "Highly focused work blocks"),
        TimerPreset("Surya Namaskar", 900L, "Sun salutation movement flow"),
        TimerPreset("Dhyana (Zen)", 1200L, "Quiet sitting meditation"),
        TimerPreset("Quick Rest", 300L, "Power nap for quick refresh")
    )

    // Selection Inputs for manual mode
    var inputHours by remember { mutableStateOf(0) }
    var inputMinutes by remember { mutableStateOf(0) }
    var inputSeconds by remember { mutableStateOf(0) }

    // Timer Running States
    var initialDurationSeconds by remember { mutableStateOf(0L) }
    var remainingSeconds by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    // Coroutine to handle countdown
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
            if (remainingSeconds == 0L) {
                isRunning = false
                isFinished = true
            }
        }
    }

    val progressRatio = if (initialDurationSeconds > 0) {
        remainingSeconds.toFloat() / initialDurationSeconds.toFloat()
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        label = "Progress bar smooth transition"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (remainingSeconds > 0 || isRunning) {
            // CountDown Screen State
            Spacer(modifier = Modifier.height(20.dp))
            
            // Active countdown styled in SoftBlueContainer with 32dp (2rem) rounded corners
            val timerBg = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                com.example.ui.theme.SoftBlueContainer
            }
            val timerTextCol = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.primary
            } else {
                com.example.ui.theme.DarkBlueText
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = timerBg
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = timerTextCol.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(10.dp)
                    ) {
                        // Background Track Arc
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = timerTextCol.copy(alpha = 0.1f),
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Foreground active Arc
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = timerTextCol,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Timer text readout inside the circle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val hrs = remainingSeconds / 3600
                            val mins = (remainingSeconds % 3600) / 60
                            val secs = remainingSeconds % 60
                            val timeString = if (hrs > 0) {
                                String.format("%02d:%02d:%02d", hrs, mins, secs)
                            } else {
                                String.format("%02d:%02d", mins, secs)
                            }

                            Text(
                                text = timeString,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = timerTextCol,
                                modifier = Modifier.testTag("timer_countdown_text")
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isRunning) "ACTIVE RUNNING" else "PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = timerTextCol.copy(alpha = 0.7f),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons for running timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = 0
                        initialDurationSeconds = 0
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("timer_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Play/Pause Button
                FilledIconButton(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("timer_play_pause_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            // Configuration Setup Screen State
            Spacer(modifier = Modifier.height(10.dp))

            // Text Banner
            Text(
                text = "Configure Countdown Timer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Standard Indian presets Grid
            Text(
                text = "Cultural Mindful Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .height(230.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets) { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                initialDurationSeconds = preset.durationSeconds
                                remainingSeconds = preset.durationSeconds
                                isRunning = true
                                isFinished = false
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val presetMinutes = preset.durationSeconds / 60
                            Text(
                                text = "$presetMinutes mins",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // manual picker section
            Text(
                text = "Custom Session Timer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Hours Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hours", style = MaterialTheme.typography.labelSmall)
                            SliderPicker(
                                value = inputHours,
                                range = 0..23,
                                onValueChange = { inputHours = it }
                            )
                        }
                        // Minutes Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minutes", style = MaterialTheme.typography.labelSmall)
                            SliderPicker(
                                value = inputMinutes,
                                range = 0..59,
                                onValueChange = { inputMinutes = it }
                            )
                        }
                        // Seconds Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Seconds", style = MaterialTheme.typography.labelSmall)
                            SliderPicker(
                                value = inputSeconds,
                                range = 0..59,
                                onValueChange = { inputSeconds = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val totalSecs = (inputHours * 3600) + (inputMinutes * 60) + inputSeconds
                            if (totalSecs > 0) {
                                initialDurationSeconds = totalSecs.toLong()
                                remainingSeconds = totalSecs.toLong()
                                isRunning = true
                                isFinished = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("timer_start_custom_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Start Custom Timer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Completion visual alert dialogue
        if (isFinished) {
            AlertDialog(
                onDismissRequest = { isFinished = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Finished",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        "Time completed!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        "Your session timer has finished successfully. Namaste!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { isFinished = false }
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SliderPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Text(
            text = String.format("%02d", value),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
