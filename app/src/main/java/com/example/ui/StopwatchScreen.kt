package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class LapRecord(
    val lapIndex: Int,
    val splitTimeFormatted: String,
    val lapTimeFormatted: String
)

@Composable
fun StopwatchScreen(
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var baseTime by remember { mutableStateOf(0L) }
    var accumulatedTime by remember { mutableStateOf(0L) }
    var currentTimeMillis by remember { mutableStateOf(0L) }

    val lapsList = remember { mutableStateListOf<LapRecord>() }
    var lastLapAccumulatedTime by remember { mutableStateOf(0L) }

    // Accurate ticking calculation
    LaunchedEffect(isRunning) {
        if (isRunning) {
            baseTime = System.currentTimeMillis() - accumulatedTime
            while (isRunning) {
                currentTimeMillis = System.currentTimeMillis() - baseTime
                accumulatedTime = currentTimeMillis
                delay(10) // tick every centisecond
            }
        }
    }

    // Centiseconds Calculation
    val totalMins = (accumulatedTime / 60000) % 60
    val totalSecs = (accumulatedTime / 1000) % 60
    val totalCentis = (accumulatedTime / 10) % 100

    val stopwatchFormatted = String.format("%02d:%02d.%02d", totalMins, totalSecs, totalCentis)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Large Digital Stopwatch Display in soft purple geometric card (32dp rounded)
        val containerBg = if (androidx.compose.foundation.isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            com.example.ui.theme.SoftPurpleContainer
        }
        val textCol = if (androidx.compose.foundation.isSystemInDarkTheme()) {
            MaterialTheme.colorScheme.primary
        } else {
            com.example.ui.theme.DarkPurpleText
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("digital_stopwatch_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerBg
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = textCol.copy(alpha = 0.15f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 44.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stopwatchFormatted,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = textCol,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("stopwatch_time_text")
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Circular Action Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Clear Button
            IconButton(
                onClick = {
                    if (isRunning) {
                        // Record Lap split
                        val currentLapIndex = lapsList.size + 1
                        val lapDiff = accumulatedTime - lastLapAccumulatedTime
                        
                        val diffMins = (lapDiff / 60000) % 60
                        val diffSecs = (lapDiff / 1000) % 60
                        val diffCentis = (lapDiff / 10) % 100
                        val lapFmt = String.format("%02d:%02d.%02d", diffMins, diffSecs, diffCentis)
                        
                        lapsList.add(
                            0, // Add to top of list for quick visibility
                            LapRecord(currentLapIndex, stopwatchFormatted, lapFmt)
                        )
                        lastLapAccumulatedTime = accumulatedTime
                    } else {
                        // Reset everything
                        accumulatedTime = 0L
                        currentTimeMillis = 0L
                        lastLapAccumulatedTime = 0L
                        lapsList.clear()
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("stopwatch_secondary_button"),
                enabled = accumulatedTime > 0
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = if (isRunning) "Lap" else "Reset",
                    tint = if (accumulatedTime > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            // Play/Pause Core Button
            FilledIconButton(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .size(72.dp)
                    .testTag("stopwatch_play_pause_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Laps Section Headers
        if (lapsList.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "LAP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "LAP TIME",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "TOTAL ELAPSED",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            // Lap List Column
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("laps_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(lapsList) { index, lap ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("#%02d", lap.lapIndex),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lap.lapTimeFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = lap.splitTimeFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Stopwatch flags",
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Press the flag button to record laps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
