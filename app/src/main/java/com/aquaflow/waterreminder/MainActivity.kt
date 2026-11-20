package com.aquaflow.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aquaflow.waterreminder.ui.CustomIntakeDialog
import com.aquaflow.waterreminder.ui.GoalDialog
import com.aquaflow.waterreminder.ui.HistoryDialog
import com.aquaflow.waterreminder.ui.HydrationViewModel
import com.aquaflow.waterreminder.ui.ReminderDialog
import com.aquaflow.waterreminder.ui.StatsDialog

private val AquaBlue = Color(0xFF087EBC)
private val DeepBlue = Color(0xFF075985)
private val PaleBlue = Color(0xFFE7F6FF)

class MainActivity : ComponentActivity() {

    private val viewModel: HydrationViewModel by viewModels {
        HydrationViewModel.provideFactory(application as AquaFlowApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AquaFlowTheme {
                AquaFlowHome(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAndResetDay()
    }
}

@Composable
private fun AquaFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AquaBlue,
            secondary = Color(0xFF43BFEA),
            background = Color(0xFFF5FBFF),
            surface = Color.White
        ),
        content = content
    )
}

@Composable
private fun AquaFlowHome(viewModel: HydrationViewModel) {
    val state by viewModel.uiState.collectAsState()
    val consumed = state.consumedMl
    val goal = state.dailyGoalMl
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "HydrationProgressAnimation"
    )

    // Listen for system date changes and time tick to reset daily intake in real-time
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                viewModel.checkAndResetDay()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    // Show undo snackbar whenever a drink is logged
    LaunchedEffect(state.lastAddedIntakeId) {
        val amount = state.lastAddedAmount
        val id = state.lastAddedIntakeId
        if (amount != null && id != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Added $amount ml of water",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastIntake()
            } else {
                viewModel.clearLastAdded()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5FBFF)
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF5FBFF)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row (Preserved exactly as original, with accessibility label)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("AquaFlow", color = DeepBlue, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                        Text("Every sip matters", color = Color(0xFF668397), fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "💧",
                            fontSize = 30.sp,
                            modifier = Modifier.clickable(onClickLabel = "View today's drink history") {
                                viewModel.setHistoryDialogVisible(true)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(34.dp))

                // Subheader with interactive access to Reminders and Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's hydration", color = Color(0xFF466477), fontSize = 17.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = if (state.streakDays > 0) "🔥 ${state.streakDays}d Stats" else "📊 Stats",
                            color = AquaBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClickLabel = "View hydration statistics") {
                                viewModel.setStatsDialogVisible(true)
                            }
                        )
                        Text(
                            text = if (state.reminderEnabled) "⏰ On" else "⏰ Reminders",
                            color = AquaBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClickLabel = "Configure water reminders") {
                                viewModel.setReminderDialogVisible(true)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Central Hydration Card (Preserved exactly as original, with tap to customize goal)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFDDF5FF), Color(0xFFB8E9FA))),
                            RoundedCornerShape(32.dp)
                        )
                        .clickable(onClickLabel = "Customize daily water goal") {
                            viewModel.setGoalDialogVisible(true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💧", fontSize = 72.sp)
                        Text("$consumed ml", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        Text(
                            text = "of $goal ml daily goal ✎",
                            color = Color(0xFF466477)
                        )
                        Spacer(Modifier.height(12.dp))
                        val percentage = (animatedProgress * 100).toInt()
                        Text(
                            text = "$percentage% completed",
                            color = DeepBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(26.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add a drink", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = DeepBlue)
                    Text(
                        text = "+ Custom",
                        color = AquaBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClickLabel = "Add custom volume drink") {
                            viewModel.setCustomIntakeDialogVisible(true)
                        }
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Quick add buttons (Preserved exactly as original, with tactile haptic feedback)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(150, 200, 250, 500).forEach { amount ->
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.addWater(amount)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaBlue)
                        ) {
                            Text("+$amount", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Text("Small sips make a big difference.", color = Color(0xFF668397), fontSize = 14.sp)

                Spacer(Modifier.height(32.dp))

                Text("Your daily hydration companion", color = Color(0xFF8AA2B1), fontSize = 12.sp)
            }
        }
    }

    // Dialogs
    if (state.showGoalDialog) {
        GoalDialog(
            currentGoalMl = state.dailyGoalMl,
            onDismiss = { viewModel.setGoalDialogVisible(false) },
            onConfirm = { newGoal ->
                viewModel.updateDailyGoal(newGoal)
                viewModel.setGoalDialogVisible(false)
            }
        )
    }

    if (state.showReminderDialog) {
        ReminderDialog(
            enabled = state.reminderEnabled,
            intervalMinutes = state.reminderIntervalMinutes,
            onDismiss = { viewModel.setReminderDialogVisible(false) },
            onSave = { enabled, interval ->
                viewModel.updateReminderSettings(enabled, interval)
                viewModel.setReminderDialogVisible(false)
            }
        )
    }

    if (state.showStatsDialog) {
        StatsDialog(
            weeklyStats = state.weeklyStats,
            streakDays = state.streakDays,
            dailyGoalMl = state.dailyGoalMl,
            onDismiss = { viewModel.setStatsDialogVisible(false) }
        )
    }

    if (state.showHistoryDialog) {
        HistoryDialog(
            intakes = state.todayIntakes,
            onDelete = { id -> viewModel.deleteIntake(id) },
            onClearAll = { viewModel.clearTodayIntakes() },
            onDismiss = { viewModel.setHistoryDialogVisible(false) }
        )
    }

    if (state.showCustomIntakeDialog) {
        CustomIntakeDialog(
            onDismiss = { viewModel.setCustomIntakeDialogVisible(false) },
            onConfirm = { amount ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.addWater(amount)
                viewModel.setCustomIntakeDialogVisible(false)
            }
        )
    }
}
