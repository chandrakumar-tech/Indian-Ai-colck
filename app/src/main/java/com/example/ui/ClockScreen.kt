package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Festival
import com.example.data.FestivalData
import com.example.data.SettingsManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockScreen(
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(SettingsManager.getAppCalendar()) }
    var showIstMode by remember { mutableStateOf(true) } // Default to true (IST Mode) so that Indian users see accurate India Time out-of-the-box!

    // Tick the clock precisely using LaunchedEffect to match system time perfectly with 0% CPU overhead
    LaunchedEffect(SettingsManager.useCustomTime, SettingsManager.timeOffsetMillis) {
        while (true) {
            currentTime = SettingsManager.getAppCalendar()
            val nextTickDelay = 1000L - (System.currentTimeMillis() % 1000L)
            delay(nextTickDelay)
        }
    }

    val displayCalendar = remember(currentTime, showIstMode) {
        if (showIstMode) {
            Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
                timeInMillis = currentTime.timeInMillis
            }
        } else {
            currentTime
        }
    }

    val timeFormatter = remember(showIstMode) {
        SimpleDateFormat("hh:mm:ss", Locale.getDefault()).apply {
            timeZone = if (showIstMode) TimeZone.getTimeZone("Asia/Kolkata") else TimeZone.getDefault()
        }
    }
    val amPmFormatter = remember(showIstMode) {
        SimpleDateFormat("a", Locale.getDefault()).apply {
            timeZone = if (showIstMode) TimeZone.getTimeZone("Asia/Kolkata") else TimeZone.getDefault()
        }
    }
    val dateFormatter = remember(showIstMode) {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).apply {
            timeZone = if (showIstMode) TimeZone.getTimeZone("Asia/Kolkata") else TimeZone.getDefault()
        }
    }

    val formattedTime = timeFormatter.format(displayCalendar.time)
    val formattedAmPm = amPmFormatter.format(displayCalendar.time)
    val formattedDate = dateFormatter.format(displayCalendar.time)

    // Trigger online holiday syncing dynamically when the active year changes
    LaunchedEffect(displayCalendar.get(Calendar.YEAR)) {
        SettingsManager.fetchOnlineHolidays(displayCalendar.get(Calendar.YEAR))
    }

    // Year-aware wrapper to resolve the correct upcoming year occurrence (prevents showing past year's dates)
    data class UpcomingFestivalInfo(
        val festival: Festival,
        val occurrenceYear: Int
    )

    // Find the next upcoming festival
    val upcomingFestivalInfo = remember(displayCalendar, SettingsManager.customFestivals, SettingsManager.onlineFestivals) {
        val currentYear = displayCalendar.get(Calendar.YEAR)
        val currentMonth = displayCalendar.get(Calendar.MONTH) + 1
        val currentDay = displayCalendar.get(Calendar.DAY_OF_MONTH)
        val allFestivals = SettingsManager.getAllFestivals()

        if (allFestivals.isEmpty()) {
            UpcomingFestivalInfo(FestivalData.list2026.first(), currentYear)
        } else {
            val futureInCurrentYear = allFestivals.firstOrNull { festival ->
                (festival.month > currentMonth) || (festival.month == currentMonth && festival.day >= currentDay)
            }
            if (futureInCurrentYear != null) {
                UpcomingFestivalInfo(futureInCurrentYear, currentYear)
            } else {
                // If all festivals for the active year have passed, the next occurrence is in the next year!
                UpcomingFestivalInfo(allFestivals.first(), currentYear + 1)
            }
        }
    }
    val upcomingFestival = upcomingFestivalInfo.festival
    val upcomingYear = upcomingFestivalInfo.occurrenceYear

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val isSmallScreen = screenHeightDp < 760

    // Adaptive design parameters to keep the layout incredibly clean and compact on small devices
    val screenPadding = if (isSmallScreen) 14.dp else 20.dp
    val topSpacerHeight = if (isSmallScreen) 4.dp else 10.dp
    val headerPaddingBottom = if (isSmallScreen) 8.dp else 16.dp
    val headerSpacerHeight = if (isSmallScreen) 6.dp else 12.dp
    val clockCardVerticalPadding = if (isSmallScreen) 16.dp else 32.dp
    val clockFontSize = if (isSmallScreen) 34.sp else 46.sp
    val clockSpacerHeight = if (isSmallScreen) 8.dp else 12.dp
    val tzSwitcherPillPaddingVertical = if (isSmallScreen) 4.dp else 6.dp
    val tzSwitcherPillPaddingHorizontal = if (isSmallScreen) 10.dp else 14.dp
    val tzSwitcherButtonPaddingVertical = if (isSmallScreen) 6.dp else 8.dp
    val tzSwitcherButtonPaddingHorizontal = if (isSmallScreen) 10.dp else 14.dp
    val mainSpacerHeight = if (isSmallScreen) 12.dp else 24.dp
    val spotlightPaddingBottom = if (isSmallScreen) 6.dp else 12.dp
    val festivalCardPadding = if (isSmallScreen) 12.dp else 18.dp
    val festivalInnerSpacer1 = if (isSmallScreen) 8.dp else 12.dp
    val festivalInnerSpacer2 = if (isSmallScreen) 8.dp else 12.dp
    val ritualBoxPadding = if (isSmallScreen) 8.dp else 10.dp
    val festivalInnerSpacer3 = if (isSmallScreen) 8.dp else 12.dp
    val wisdomSpacerHeight = if (isSmallScreen) 16.dp else 30.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(topSpacerHeight))

        // Top header label: Date in uppercase and IndicTime title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = headerPaddingBottom),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = formattedDate.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "IndicTime Clock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(headerSpacerHeight))

        // Large Digital Clock Plate with 2rem (32dp) rounded corners matching the Geometric theme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("digital_clock_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = clockCardVerticalPadding, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large styled monospaced clock display
                Text(
                    text = "$formattedTime $formattedAmPm".uppercase(),
                    fontSize = clockFontSize,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-1).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("digital_clock_text")
                )

                Spacer(modifier = Modifier.height(clockSpacerHeight))

                // Timezone pill badge
                val activeTz = if (showIstMode) {
                    TimeZone.getTimeZone("Asia/Kolkata")
                } else {
                    TimeZone.getDefault()
                }
                val activeTzName = if (showIstMode) "Asia/Kolkata (IST)" else activeTz.id
                val activeRawOffset = activeTz.getOffset(currentTime.timeInMillis)
                val activeOffsetHours = activeRawOffset / 1000 / 60 / 60
                val activeOffsetMinutes = Math.abs((activeRawOffset / 1000 / 60) % 60)
                val activeOffsetStr = String.format("GMT %+d:%02d", activeOffsetHours, activeOffsetMinutes)

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = tzSwitcherPillPaddingHorizontal, vertical = tzSwitcherPillPaddingVertical)
                ) {
                    Text(
                        text = "$activeTzName • $activeOffsetStr".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(if (isSmallScreen) 10.dp else 16.dp))

                // Timezone switcher Toggle Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .clickable { showIstMode = !showIstMode }
                        .padding(horizontal = tzSwitcherButtonPaddingHorizontal, vertical = tzSwitcherButtonPaddingVertical)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Timezone Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showIstMode) "Switch to Device Time" else "Switch to India Time (IST)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(mainSpacerHeight))

        // Upcoming Festival Spotlight Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = spotlightPaddingBottom),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Festival Spotlight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dynamic live online sync state badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (SettingsManager.isFetchingOnline) MaterialTheme.colorScheme.secondaryContainer
                        else if (SettingsManager.onlineFestivals.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                if (SettingsManager.isFetchingOnline) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SYNCING...",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else if (SettingsManager.onlineFestivals.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Online logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ONLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = "OFFLINE SOURCE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upcoming_festival_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(festivalCardPadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Festival",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = upcomingFestival.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = upcomingFestival.getFormattedDate(upcomingYear),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Festival Type Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = upcomingFestival.type.label.take(15) + "...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(festivalInnerSpacer1))

                Text(
                    text = upcomingFestival.description,
                    style = if (isSmallScreen) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    lineHeight = if (isSmallScreen) 17.sp else 20.sp
                )

                Spacer(modifier = Modifier.height(festivalInnerSpacer2))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(ritualBoxPadding)
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Activity Tip",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Celebration Ritual / Tip",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = upcomingFestival.ritualOrTip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(festivalInnerSpacer3))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (SettingsManager.isFetchingOnline) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Syncing AI...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        TextButton(
                            onClick = {
                                SettingsManager.fetchOnlineHolidays(displayCalendar.get(Calendar.YEAR))
                            },
                            modifier = Modifier.testTag("sync_ai_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync AI",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sync AI",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onNavigateToCalendar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("view_calendar_button")
                    ) {
                        Text("View Full Indian Calendar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(wisdomSpacerHeight))

        // Culture Wisdom Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Wisdom quote icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "\"Kaal kare so aaj kar, aaj kare so ab.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Do tomorrow's work today, and today's work now. Time is precious.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
