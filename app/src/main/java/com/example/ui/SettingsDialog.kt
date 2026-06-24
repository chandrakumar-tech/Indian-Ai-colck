package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Festival
import com.example.data.FestivalType
import com.example.data.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSection by remember { mutableStateOf(0) } // 0 = Date & Time, 1 = Festivals & Holidays

    // --- Time Override Form State ---
    val currentCal = remember { SettingsManager.getAppCalendar() }
    var selectedYear by remember { mutableStateOf(currentCal.get(Calendar.YEAR).toString()) }
    var selectedMonth by remember { mutableStateOf(currentCal.get(Calendar.MONTH)) } // 0-indexed
    var selectedDay by remember { mutableStateOf(currentCal.get(Calendar.DAY_OF_MONTH).toString()) }
    var selectedHour by remember { mutableStateOf(currentCal.get(Calendar.HOUR).toString()) } // 1-12
    var selectedMinute by remember { mutableStateOf(String.format("%02d", currentCal.get(Calendar.MINUTE))) }
    var selectedAmPm by remember { mutableStateOf(if (currentCal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM") }

    // Dropdowns open/close state
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var amPmDropdownExpanded by remember { mutableStateOf(false) }

    // --- Custom Festival Form State ---
    var isAddingFestival by remember { mutableStateOf(false) }
    var editingFestivalName by remember { mutableStateOf<String?>(null) } // Set if editing an existing festival

    var festName by remember { mutableStateOf("") }
    var festMonth by remember { mutableStateOf(6) } // Default June
    var festDay by remember { mutableStateOf(24) }
    var festType by remember { mutableStateOf(FestivalType.REGIONAL) }
    var festDesc by remember { mutableStateOf("") }
    var festRitual by remember { mutableStateOf("") }

    var festMonthDropdownExpanded by remember { mutableStateOf(false) }
    var festTypeDropdownExpanded by remember { mutableStateOf(false) }

    // List of months for selections
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Current lists of custom festivals
    val customFestivalsList = SettingsManager.customFestivals

    // Save states or display messages
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Custom full width dialog
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp), // Leaves room for top status bar
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Settings Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Samay Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Settings")
                    }
                }

                // Dynamic Status Message Banner
                if (statusMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { statusMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Status",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Top Tab Selector for Settings Sections
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf("Date & Time Override", "Edit Festivals")
                    tabs.forEachIndexed { index, label ->
                        val isSelected = activeSection == index
                        val tabBg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val tabTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tabBg)
                                .clickable { activeSection = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = tabTextColor
                            )
                        }
                    }
                }

                // Content scroll area based on active section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (activeSection == 0) {
                        // Section 0: DATE & TIME OVERRIDE
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Current Active Simulated Time Status Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (SettingsManager.useCustomTime) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (SettingsManager.useCustomTime) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (SettingsManager.useCustomTime) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Clock",
                                            tint = if (SettingsManager.useCustomTime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (SettingsManager.useCustomTime) "Custom Override Active" else "Using Device Live Time",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (SettingsManager.useCustomTime) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        val formattedTime = remember(SettingsManager.useCustomTime, SettingsManager.timeOffsetMillis) {
                                            val cal = SettingsManager.getAppCalendar()
                                            val formatter = SimpleDateFormat("hh:mm:ss a • MMMM dd, yyyy", Locale.getDefault())
                                            formatter.format(cal.time)
                                        }

                                        Text(
                                            text = formattedTime,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Setup Custom App Time & Date",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Set any custom day or year (like year 2026) to test holidays or align the app with a specific cultural calendar context.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom Input Fields Grid
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // Row 1: Year, Month, Day inputs
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Year Input
                                        OutlinedTextField(
                                            value = selectedYear,
                                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) selectedYear = it },
                                            label = { Text("Year") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // Month Dropdown Select Box
                                        Box(
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .clickable { monthDropdownExpanded = true }
                                        ) {
                                            OutlinedTextField(
                                                value = monthNames[selectedMonth],
                                                onValueChange = {},
                                                label = { Text("Month") },
                                                readOnly = true,
                                                enabled = false,
                                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Open Dropdown") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            DropdownMenu(
                                                expanded = monthDropdownExpanded,
                                                onDismissRequest = { monthDropdownExpanded = false },
                                                modifier = Modifier.fillMaxWidth(0.45f)
                                            ) {
                                                monthNames.forEachIndexed { idx, name ->
                                                    DropdownMenuItem(
                                                        text = { Text(name) },
                                                        onClick = {
                                                            selectedMonth = idx
                                                            monthDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Day Input
                                        OutlinedTextField(
                                            value = selectedDay,
                                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) selectedDay = it },
                                            label = { Text("Day") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }

                                    // Row 2: Hour, Minute, AM/PM Inputs
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Hour Input
                                        OutlinedTextField(
                                            value = selectedHour,
                                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) selectedHour = it },
                                            label = { Text("Hour") },
                                            placeholder = { Text("12") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // Minute Input
                                        OutlinedTextField(
                                            value = selectedMinute,
                                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) selectedMinute = it },
                                            label = { Text("Minute") },
                                            placeholder = { Text("00") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        // AM/PM Selector Dropdown
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { amPmDropdownExpanded = true }
                                        ) {
                                            OutlinedTextField(
                                                value = selectedAmPm,
                                                onValueChange = {},
                                                label = { Text("AM/PM") },
                                                readOnly = true,
                                                enabled = false,
                                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Open") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            DropdownMenu(
                                                expanded = amPmDropdownExpanded,
                                                onDismissRequest = { amPmDropdownExpanded = false }
                                            ) {
                                                listOf("AM", "PM").forEach { item ->
                                                    DropdownMenuItem(
                                                        text = { Text(item) },
                                                        onClick = {
                                                            selectedAmPm = item
                                                            amPmDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Time Action Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Apply Button
                                Button(
                                    onClick = {
                                        try {
                                            val y = selectedYear.toIntOrNull() ?: 2026
                                            val m = selectedMonth // 0-indexed already
                                            val d = selectedDay.toIntOrNull() ?: 1
                                            
                                            var h = selectedHour.toIntOrNull() ?: 12
                                            val min = selectedMinute.toIntOrNull() ?: 0

                                            // Convert 12 hour to 24 hour calendar standard
                                            if (selectedAmPm.uppercase() == "PM" && h < 12) h += 12
                                            if (selectedAmPm.uppercase() == "AM" && h == 12) h = 0

                                            val targetCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                                set(Calendar.HOUR_OF_DAY, h)
                                                set(Calendar.MINUTE, min)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }

                                            SettingsManager.setCustomDateTime(targetCal)
                                            statusMessage = "Application clock override set to ${selectedHour}:${String.format("%02d", min)} ${selectedAmPm} on ${monthNames[m]} ${d}, ${y}."
                                        } catch (e: Exception) {
                                            statusMessage = "Error setting time: Make sure inputs are valid numbers!"
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Check, "Apply")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply Custom Time", fontWeight = FontWeight.Bold)
                                }

                                // Reset Button
                                OutlinedButton(
                                    onClick = {
                                        SettingsManager.resetToSystemTime()
                                        // Reset fields to current system time
                                        val nowCal = Calendar.getInstance()
                                        selectedYear = nowCal.get(Calendar.YEAR).toString()
                                        selectedMonth = nowCal.get(Calendar.MONTH)
                                        selectedDay = nowCal.get(Calendar.DAY_OF_MONTH).toString()
                                        selectedHour = nowCal.get(Calendar.HOUR).toString()
                                        selectedMinute = String.format("%02d", nowCal.get(Calendar.MINUTE))
                                        selectedAmPm = if (nowCal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
                                        statusMessage = "Reset app to actual live device system time successfully."
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Refresh, "Reset")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Live", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Section 1: FESTIVALS & HOLIDAYS EDITING
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            if (!isAddingFestival) {
                                // List of currently configured/custom festivals
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Manage Custom Holidays",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Add new local holidays or edit existing ones. Matches by name to edit/override standard festivals.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            // Open Add form
                                            festName = ""
                                            festMonth = 6
                                            festDay = 24
                                            festType = FestivalType.REGIONAL
                                            festDesc = ""
                                            festRitual = ""
                                            editingFestivalName = null
                                            isAddingFestival = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "Add")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // List of Custom items in LazyColumn
                                if (customFestivalsList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = "No custom items",
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "No custom holidays added yet",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Click the 'Add' button to create a custom local holiday or to customize/override any default festival.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(customFestivalsList) { fest ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = fest.name,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "${monthNames[fest.month - 1]} ${fest.day} • ${fest.type.label}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        // Edit Button
                                                        IconButton(
                                                            onClick = {
                                                                festName = fest.name
                                                                festMonth = fest.month
                                                                festDay = fest.day
                                                                festType = fest.type
                                                                festDesc = fest.description
                                                                festRitual = fest.ritualOrTip
                                                                editingFestivalName = fest.name
                                                                isAddingFestival = true
                                                            }
                                                        ) {
                                                            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                                        }

                                                        // Delete Button
                                                        IconButton(
                                                            onClick = {
                                                                SettingsManager.deleteCustomFestival(fest.name)
                                                                statusMessage = "Deleted holiday '${fest.name}' successfully."
                                                            }
                                                        ) {
                                                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Add/Edit Form Mode (Vertical Scrollable form)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = if (editingFestivalName != null) "Edit Custom Holiday" else "Add Custom Holiday",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Name Field
                                    OutlinedTextField(
                                        value = festName,
                                        onValueChange = { festName = it },
                                        label = { Text("Festival / Holiday Name") },
                                        placeholder = { Text("e.g. My Birthday Holiday") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Month Selection Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Month Selector
                                        Box(
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .clickable { festMonthDropdownExpanded = true }
                                        ) {
                                            OutlinedTextField(
                                                value = monthNames[festMonth - 1],
                                                onValueChange = {},
                                                label = { Text("Holiday Month") },
                                                readOnly = true,
                                                enabled = false,
                                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Open") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            DropdownMenu(
                                                expanded = festMonthDropdownExpanded,
                                                onDismissRequest = { festMonthDropdownExpanded = false }
                                            ) {
                                                monthNames.forEachIndexed { idx, name ->
                                                    DropdownMenuItem(
                                                        text = { Text(name) },
                                                        onClick = {
                                                            festMonth = idx + 1
                                                            festMonthDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Day Selector
                                        OutlinedTextField(
                                            value = festDay.toString(),
                                            onValueChange = {
                                                val num = it.toIntOrNull()
                                                if (num != null && num in 1..31) {
                                                    festDay = num
                                                } else if (it.isEmpty()) {
                                                    festDay = 1
                                                }
                                            },
                                            label = { Text("Holiday Day") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }

                                    // Holiday Type Dropdown Selector
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { festTypeDropdownExpanded = true }
                                    ) {
                                        OutlinedTextField(
                                            value = festType.label,
                                            onValueChange = {},
                                            label = { Text("Holiday Category / Type") },
                                            readOnly = true,
                                            enabled = false,
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Open") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                disabledBorderColor = MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        DropdownMenu(
                                            expanded = festTypeDropdownExpanded,
                                            onDismissRequest = { festTypeDropdownExpanded = false }
                                        ) {
                                            FestivalType.values().forEach { type ->
                                                DropdownMenuItem(
                                                    text = { Text(type.label) },
                                                    onClick = {
                                                        festType = type
                                                        festTypeDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Description Field
                                    OutlinedTextField(
                                        value = festDesc,
                                        onValueChange = { festDesc = it },
                                        label = { Text("Description") },
                                        placeholder = { Text("What is this holiday celebrated for?") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Ritual / Custom Tip Field
                                    OutlinedTextField(
                                        value = festRitual,
                                        onValueChange = { festRitual = it },
                                        label = { Text("Celebratory Custom / Act (Ritual)") },
                                        placeholder = { Text("e.g. Cut cakes, spend sweet moments, wear white garments") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Form Action Buttons Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Save Button
                                        Button(
                                            onClick = {
                                                if (festName.isBlank()) {
                                                    statusMessage = "Error: Holiday Name cannot be empty!"
                                                    return@Button
                                                }

                                                // Format a simple custom date string representation
                                                val dateStr = "${monthNames[festMonth - 1]} ${String.format("%02d", festDay)}, 2026"

                                                val fest = Festival(
                                                    name = festName.trim(),
                                                    dateStr = dateStr,
                                                    month = festMonth,
                                                    day = festDay,
                                                    type = festType,
                                                    description = festDesc.trim().ifEmpty { "A custom celebrated festive day." },
                                                    ritualOrTip = festRitual.trim().ifEmpty { "Celebrate and enjoy personal reflection or sweet family time!" }
                                                )

                                                SettingsManager.addCustomFestival(fest)
                                                statusMessage = if (editingFestivalName != null) {
                                                    "Successfully updated custom holiday '${fest.name}'."
                                                } else {
                                                    "Successfully added new custom holiday '${fest.name}'."
                                                }
                                                isAddingFestival = false
                                            },
                                            modifier = Modifier.weight(1.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Check, "Save")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save Holiday", fontWeight = FontWeight.Bold)
                                        }

                                        // Cancel Button
                                        OutlinedButton(
                                            onClick = { isAddingFestival = false },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Cancel", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
