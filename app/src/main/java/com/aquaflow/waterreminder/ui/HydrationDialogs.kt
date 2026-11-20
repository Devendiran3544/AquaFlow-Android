package com.aquaflow.waterreminder.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aquaflow.waterreminder.data.WaterIntakeEntity
import com.aquaflow.waterreminder.domain.DayHydrationStat
import com.aquaflow.waterreminder.domain.HydrationCalculator

private val AquaBlue = Color(0xFF087EBC)
private val DeepBlue = Color(0xFF075985)
private val PaleBlue = Color(0xFFE7F6FF)

@Composable
fun GoalDialog(
    currentGoalMl: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoalMl.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Customise Daily Goal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Recommended daily intake varies between 1500 ml and 3500 ml based on activity.",
                    fontSize = 13.sp,
                    color = Color(0xFF668397)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Goal (ml)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1500, 2000, 2500, 3000).forEach { preset ->
                        AssistChip(
                            onClick = { goalText = preset.toString() },
                            label = { Text("${preset}ml", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF668397))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newGoal = goalText.toIntOrNull()?.coerceIn(500, 10000) ?: currentGoalMl
                            onConfirm(newGoal)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Goal")
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderDialog(
    enabled: Boolean,
    intervalMinutes: Int,
    soundEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (Boolean, Int, Boolean) -> Unit
) {
    var reminderEnabled by remember { mutableStateOf(enabled) }
    var reminderSound by remember { mutableStateOf(soundEnabled) }
    var selectedInterval by remember { mutableIntStateOf(intervalMinutes) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                reminderEnabled = false
            }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Water Reminders",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Reminders", fontWeight = FontWeight.SemiBold, color = DeepBlue)
                        Text(
                            "Get notified during active hours",
                            fontSize = 12.sp,
                            color = Color(0xFF668397)
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { checked ->
                            reminderEnabled = checked
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AquaBlue)
                    )
                }

                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Water Drop Sound", fontWeight = FontWeight.SemiBold, color = DeepBlue)
                            Text(
                                "Play custom water drop chime",
                                fontSize = 12.sp,
                                color = Color(0xFF668397)
                            )
                        }
                        Switch(
                            checked = reminderSound,
                            onCheckedChange = { reminderSound = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AquaBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text("Reminder Interval", fontWeight = FontWeight.SemiBold, color = DeepBlue, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val intervals = listOf(30, 45, 60, 90, 120)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        intervals.forEach { mins ->
                            FilterChip(
                                selected = (selectedInterval == mins),
                                onClick = { selectedInterval = mins },
                                label = { Text("${mins}m", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Reminders pause automatically once you hit today's goal and during sleep hours (10 PM – 8 AM).",
                        fontSize = 12.sp,
                        color = Color(0xFF668397)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF668397))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(reminderEnabled, selectedInterval, reminderSound)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun StatsDialog(
    weeklyStats: List<DayHydrationStat>,
    streakDays: Int = 0,
    dailyGoalMl: Int,
    onDismiss: () -> Unit
) {
    val totalWeeklyConsumed = weeklyStats.sumOf { it.totalConsumedMl }
    val averageDaily = if (weeklyStats.isNotEmpty()) totalWeeklyConsumed / weeklyStats.size else 0
    val daysGoalMet = weeklyStats.count { it.totalConsumedMl >= dailyGoalMl }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Hydration Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Summary Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F8FF), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥 Streak", fontSize = 11.sp, color = Color(0xFF668397))
                        Text("${streakDays}d", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Avg Daily", fontSize = 11.sp, color = Color(0xFF668397))
                        Text("${averageDaily}ml", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("7-Day Total", fontSize = 11.sp, color = Color(0xFF668397))
                        Text("${totalWeeklyConsumed}ml", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Goal Met", fontSize = 11.sp, color = Color(0xFF668397))
                        Text("$daysGoalMet/7", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Last 7 Days", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DeepBlue)
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    weeklyStats.forEach { stat ->
                        val ratio = (stat.totalConsumedMl.toFloat() / stat.goalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${stat.dayLabel} (${stat.dateString.takeLast(5)})",
                                    fontSize = 12.sp,
                                    color = if (stat.dayLabel == "Today") DeepBlue else Color(0xFF668397),
                                    fontWeight = if (stat.dayLabel == "Today") FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${stat.totalConsumedMl} / ${stat.goalMl} ml (${stat.percentage}%)",
                                    fontSize = 12.sp,
                                    color = if (stat.totalConsumedMl >= stat.goalMl) AquaBlue else Color(0xFF466477),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (stat.totalConsumedMl >= stat.goalMl) Color(0xFF10B981) else AquaBlue,
                                trackColor = Color(0xFFE2EFF7),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AquaBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryDialog(
    intakes: List<WaterIntakeEntity>,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Intake Log",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlue
                    )
                    if (intakes.isNotEmpty()) {
                        TextButton(onClick = onClearAll) {
                            Text("Clear All", color = Color(0xFFE53935), fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (intakes.isEmpty()) "No drinks logged yet today." else "Tap a record to remove it if logged accidentally.",
                    fontSize = 12.sp,
                    color = Color(0xFF668397)
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (intakes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💧 Drink water to log your first sip!", color = Color(0xFF8AA2B1))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(intakes, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5FBFF), RoundedCornerShape(12.dp))
                                    .clickable { onDelete(item.id) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💧", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "+${item.amountMl} ml",
                                            fontWeight = FontWeight.Bold,
                                            color = DeepBlue,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = HydrationCalculator.formatTime(item.timestamp),
                                            fontSize = 11.sp,
                                            color = Color(0xFF668397)
                                        )
                                    }
                                }
                                Text("Remove", color = Color(0xFFE53935), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AquaBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomIntakeDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("250") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Custom Water Intake",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Log any specific volume (e.g. coffee cup, bottle, or tumbler).",
                    fontSize = 13.sp,
                    color = Color(0xFF668397)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Volume (ml)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100, 250, 330, 750).forEach { preset ->
                        AssistChip(
                            onClick = { amountText = preset.toString() },
                            label = { Text("${preset}ml", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF668397))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountText.toIntOrNull()?.let { HydrationCalculator.sanitizeIntakeAmount(it) } ?: 250
                            onConfirm(amount)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AquaBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Intake")
                    }
                }
            }
        }
    }
}
