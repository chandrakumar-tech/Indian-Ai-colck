package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Festival
import com.example.data.FestivalData
import com.example.data.FestivalType
import com.example.data.SettingsManager
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier
) {
    val today = remember(SettingsManager.useCustomTime, SettingsManager.timeOffsetMillis) {
        SettingsManager.getAppCalendar()
    }

    var year by remember(today) { mutableStateOf(today.get(Calendar.YEAR)) }
    var selectedMonthIndex by remember(today) { mutableStateOf(today.get(Calendar.MONTH)) } // 0 = Jan, 11 = Dec

    val isTodayInSelectedMonth = remember(selectedMonthIndex, year, today) {
        today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == selectedMonthIndex
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFestivalDetail by remember { mutableStateOf<Festival?>(null) }

    // Active selected day of the month (defaults to null to display all month's festivals by default)
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Sync selected day when month or year changes (clear selection to show new month's full festivals list)
    LaunchedEffect(selectedMonthIndex, year, today) {
        selectedDay = null
    }

    // Dynamic online sync when active year on Calendar changes
    LaunchedEffect(year) {
        SettingsManager.fetchOnlineHolidays(year)
    }

    // Month Calculations
    val cal = remember(year, selectedMonthIndex) {
        GregorianCalendar(year, selectedMonthIndex, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 7 = Sat
    val totalDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Filter festivals by selected month
    val currentMonthFestivals = remember(selectedMonthIndex, SettingsManager.customFestivals, SettingsManager.onlineFestivals) {
        SettingsManager.getFestivalsForMonth(selectedMonthIndex + 1)
    }

    // Filter festivals for selected day (if a day is selected, otherwise show full month)
    val displayedFestivals = remember(selectedDay, currentMonthFestivals) {
        if (selectedDay == null) currentMonthFestivals
        else currentMonthFestivals.filter { it.day == selectedDay }
    }

    // Filter festivals globally by search query
    val searchedFestivals = remember(searchQuery, SettingsManager.customFestivals, SettingsManager.onlineFestivals) {
        if (searchQuery.isBlank()) emptyList()
        else {
            SettingsManager.getAllFestivals().filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Days Grid Matrix (including leading offsets and trailing alignment padding)
    val dayCells = remember(firstDayOfWeek, totalDaysInMonth) {
        val list = mutableListOf<Int?>()
        // Add nulls for calendar offsets
        for (i in 1 until firstDayOfWeek) {
            list.add(null)
        }
        // Add actual days
        for (d in 1..totalDaysInMonth) {
            list.add(d)
        }
        // Perfect layout grid: pad the end with nulls to make it a multiple of 7
        while (list.size % 7 != 0) {
            list.add(null)
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Search Bar for Year 2026 Festivals
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search any festival of 2026... (e.g. Diwali)") },
            leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Clear Icon")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("festival_search_bar"),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotEmpty()) {
            // Search Results Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Results (${searchedFestivals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { searchQuery = "" }) {
                    Text("Cancel Search")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (searchedFestivals.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchedFestivals) { fest ->
                        FestivalListItem(
                            festival = fest,
                            year = year,
                            onClick = { selectedFestivalDetail = fest }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "Not Found",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No festivals found matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

        } else {
            // Normal Calendar Mode
            // Month Switcher Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (selectedMonthIndex > 0) {
                            selectedMonthIndex -= 1
                        } else {
                            selectedMonthIndex = 11 // wrap to Dec
                            year -= 1
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.ChevronLeft, "Prev Month", tint = MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = "${monthNames[selectedMonthIndex]} $year",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("calendar_month_title")
                )

                IconButton(
                    onClick = {
                        if (selectedMonthIndex < 11) {
                            selectedMonthIndex += 1
                        } else {
                            selectedMonthIndex = 0 // wrap to Jan
                            year += 1
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.ChevronRight, "Next Month", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Grid Area Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Week Days Header
                    val weekHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        weekHeaders.forEach { wh ->
                            Text(
                                text = wh,
                                modifier = Modifier.width(42.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (wh == "S" || wh == "S") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid Drawing
                    val chunkedCells = dayCells.chunked(7)
                    chunkedCells.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { day ->
                                if (day == null) {
                                    Spacer(modifier = Modifier.size(42.dp))
                                } else {
                                    // Assess holidays for today
                                    val dayFestivals = currentMonthFestivals.filter { it.day == day }
                                    val isHoliday = dayFestivals.isNotEmpty()
                                    val isToday = isTodayInSelectedMonth && (day == today.get(Calendar.DAY_OF_MONTH))
                                    val isSelected = (selectedDay == day)

                                    // Dynamic styling with highly custom Material 3 palettes
                                    val cellBgColor = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        isHoliday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                                        else -> Color.Transparent
                                    }
                                    val cellTextColor = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.primary
                                        isHoliday -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                    val cellBorderColor = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary
                                        isHoliday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                        else -> Color.Transparent
                                    }
                                    val cellBorderWidth = when {
                                        isSelected -> 0.dp
                                        isToday -> 1.5.dp
                                        isHoliday -> 1.dp
                                        else -> 0.dp
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(cellBgColor)
                                            .border(
                                                width = cellBorderWidth,
                                                color = cellBorderColor,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedDay = if (selectedDay == day) null else day
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected || isToday || isHoliday) FontWeight.Bold else FontWeight.Normal,
                                                color = cellTextColor
                                            )
                                            // Small indicator dot below numbers
                                            if (isHoliday) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                            else MaterialTheme.colorScheme.secondary
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            // Fill remaining spaces in short weeks
                            if (week.size < 7) {
                                for (k in 0 until (7 - week.size)) {
                                    Spacer(modifier = Modifier.size(42.dp))
                                }
                            }
                        }
                    }

                    // Legend Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Legend: Selected
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Selected", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Legend: Today
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Today", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Legend: Holiday
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                    .border(0.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Holiday", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unified Header Row (Dynamic depending on selection)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDay != null) {
                        "Festivals on ${monthNames[selectedMonthIndex]} $selectedDay"
                    } else {
                        "All Festivals in ${monthNames[selectedMonthIndex]}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (selectedDay != null) {
                    TextButton(
                        onClick = { selectedDay = null },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear selected day filter",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Show All Month",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (displayedFestivals.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedFestivals) { fest ->
                        FestivalListItem(
                            festival = fest,
                            year = year,
                            onClick = { selectedFestivalDetail = fest }
                        )
                    }
                }
            } else {
                // Beautiful Empty State Card as explicitly requested by the user
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "No Events Icon",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedDay != null) "No Festival or Holiday" else "No Holidays recorded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedDay != null) {
                                "No traditional festivals or public holidays are recorded for ${monthNames[selectedMonthIndex]} $selectedDay, $year."
                            } else {
                                "No traditional festivals or public holidays are recorded for ${monthNames[selectedMonthIndex]} $year."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Details Alert Dialog
        selectedFestivalDetail?.let { festival ->
            AlertDialog(
                onDismissRequest = { selectedFestivalDetail = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = festival.name,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date String Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = festival.getFormattedDate(year),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Category Tag
                        val categoryColor = when (festival.type) {
                            FestivalType.NATIONAL -> Color(0xFF1E88E5)
                            FestivalType.GAZETTED -> Color(0xFFD84315)
                            FestivalType.RESTRICTED -> Color(0xFFE65100)
                            FestivalType.REGIONAL -> Color(0xFF43A047)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(categoryColor.copy(alpha = 0.12f))
                                .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = festival.type.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = categoryColor
                            )
                        }

                        // Description
                        Text(
                            text = festival.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )

                        // Ritual Custom Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = "Rituals",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Celebratory Custom / Act",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = festival.ritualOrTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedFestivalDetail = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun FestivalListItem(
    festival: Festival,
    year: Int,
    onClick: () -> Unit
) {
    val categoryColor = when (festival.type) {
        FestivalType.NATIONAL -> Color(0xFF1E88E5)
        FestivalType.GAZETTED -> Color(0xFFD84315)
        FestivalType.RESTRICTED -> Color(0xFFE65100)
        FestivalType.REGIONAL -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("festival_list_item_${festival.name.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color accent bar based on category
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(72.dp)
                    .background(categoryColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = festival.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date icon",
                            tint = categoryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = festival.getFormattedDate(year),
                            style = MaterialTheme.typography.bodySmall,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Arrow circle icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Info details arrow",
                        tint = categoryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
