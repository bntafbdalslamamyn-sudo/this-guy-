package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DailyStatsItem
import com.example.data.model.ExerciseItem
import com.example.data.model.WorkoutLogItem
import com.example.ui.components.BodyCompleteBrandHeader
import com.example.ui.theme.CalorieFlameGold
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.ElevatedCardSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MutedSteelText
import com.example.ui.theme.NeonCyanAccent
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhiteText

@Composable
fun ProgressStatsScreen(
    exercises: List<ExerciseItem>,
    workoutLogs: List<WorkoutLogItem>,
    todayStats: DailyStatsItem,
    isArabic: Boolean,
    onSaveBodyMetrics: (
        bodyWeightKg: Double,
        chestCm: Double,
        armCm: Double,
        waistCm: Double,
        thighCm: Double,
        stepsCount: Long,
        sleepHours: Double
    ) -> Unit,
    onToggleLanguage: () -> Unit,
    onSignOut: () -> Unit
) {
    var chartVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { chartVisible = true }

    val chartAnimFactor by animateFloatAsState(
        targetValue = if (chartVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "barChartAnim"
    )

    var weightStr by remember(todayStats.bodyWeightKg) { mutableStateOf(todayStats.bodyWeightKg.toString()) }
    var chestStr by remember(todayStats.chestCm) { mutableStateOf(todayStats.chestCm.toString()) }
    var armStr by remember(todayStats.armCm) { mutableStateOf(todayStats.armCm.toString()) }
    var waistStr by remember(todayStats.waistCm) { mutableStateOf(todayStats.waistCm.toString()) }
    var thighStr by remember(todayStats.thighCm) { mutableStateOf(todayStats.thighCm.toString()) }
    var stepsStr by remember(todayStats.stepsCount) { mutableStateOf(todayStats.stepsCount.toString()) }
    var sleepStr by remember(todayStats.sleepHours) { mutableStateOf(todayStats.sleepHours.toString()) }

    val totalCompletedSets = workoutLogs.sumOf { it.completedSets }
    val totalMinutes = workoutLogs.sumOf { it.durationMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("progress_stats_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header + Language & Sign Out controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "متابعة الأداء والتقدم" else "Performance & Progress",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhiteText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "سجل الأوزان الحقيقية والقياسات الجسدية" else "Real Workout & Body Metrics Log",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonCyanAccent
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("toggle_language_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = NeonCyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "EN" else "عربي", color = PureWhiteText)
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "تسجيل الخروج",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. Celebratory Weekly Achievement & Real Stats Summary
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ElevatedCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = CalorieFlameGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ملخص الالتزام والأداء الفعلي",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "كل تمرين تكمله يتم حفظه ومزامنته مباشرة مع حسابك.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSteelText
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatPill(
                        label = "التمارين المسجلة",
                        value = "${workoutLogs.size}",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatPill(
                        label = "المجموعات المكتملة",
                        value = "$totalCompletedSets",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatPill(
                        label = "إجمالي الدقائق",
                        value = "${totalMinutes}د",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Animated Working Weights Progress Bar Chart ("مخططات التقدم المتحركة")
        item {
            val topExercises = exercises.take(6)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timeline, contentDescription = null, tint = NeonCyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مخطط تطور الأوزان في التمارين الأساسية (كجم)",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhiteText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val maxWeight = (topExercises.maxOfOrNull { it.lastWeightKg } ?: 100.0).coerceAtLeast(60.0)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    val count = topExercises.size.coerceAtLeast(1)
                    val slotWidth = size.width / count
                    val barWidth = (slotWidth * 0.52f).coerceAtMost(42.dp.toPx())

                    topExercises.forEachIndexed { idx, ex ->
                        val ratio = ((ex.lastWeightKg / maxWeight).toFloat() * chartAnimFactor).coerceIn(0.08f, 1f)
                        val barHeight = size.height * ratio
                        val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                        val top = size.height - barHeight

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonCyanAccent, ElectricBluePrimary)
                            ),
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    topExercises.forEach { ex ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${ex.lastWeightKg.toInt()}kg",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonCyanAccent
                            )
                            Text(
                                text = ex.muscleGroupAr.take(8),
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedSteelText
                            )
                        }
                    }
                }
            }
        }

        // 4. Body Weight & Body Measurements Form ("الوزن الجسدي والقياسات الجسمية")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = ElectricBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل الوزن والقياسات الجسمية",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhiteText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("الوزن (كجم)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("body_weight_input")
                    )
                    OutlinedTextField(
                        value = chestStr,
                        onValueChange = { chestStr = it },
                        label = { Text("الصدر (سم)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = armStr,
                        onValueChange = { armStr = it },
                        label = { Text("الذراع (سم)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = waistStr,
                        onValueChange = { waistStr = it },
                        label = { Text("الخصر (سم)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = thighStr,
                        onValueChange = { thighStr = it },
                        label = { Text("الفخذ (سم)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stepsStr,
                        onValueChange = { stepsStr = it },
                        label = { Text("الخطوات اليوم") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sleepStr,
                        onValueChange = { sleepStr = it },
                        label = { Text("النوم (ساعات)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        onSaveBodyMetrics(
                            weightStr.toDoubleOrNull() ?: todayStats.bodyWeightKg,
                            chestStr.toDoubleOrNull() ?: todayStats.chestCm,
                            armStr.toDoubleOrNull() ?: todayStats.armCm,
                            waistStr.toDoubleOrNull() ?: todayStats.waistCm,
                            thighStr.toDoubleOrNull() ?: todayStats.thighCm,
                            stepsStr.toLongOrNull() ?: todayStats.stepsCount,
                            sleepStr.toDoubleOrNull() ?: todayStats.sleepHours
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_body_metrics_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ القياسات الجسدية")
                }
            }
        }

        // 5. Recorded Workout Logs History
        item {
            Text(
                text = "سجل التمارين المكتملة",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhiteText,
                fontWeight = FontWeight.Bold
            )
        }

        if (workoutLogs.isEmpty()) {
            item {
                Surface(
                    color = DarkCardSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = NeonCyanAccent,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لم يتم تسجيل جلسات تمرين بعد. افتح أي تمرين واضغط «إنهاء التمرين وحفظ النتائج» ليظهر سجلك الفعلي هنا.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSteelText
                        )
                    }
                }
            }
        } else {
            items(workoutLogs, key = { it.id }) { log ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCardSurface)
                        .border(1.dp, ElectricBluePrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = log.exerciseNameAr,
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${log.weightKg} كجم",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${log.dayTitleAr} • ${log.completedSets}/${log.targetSets} مجموعات • التكرارات: ${log.repsCompleted} • المدة: ${log.durationMinutes} دقيقة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonCyanAccent
                    )
                    if (log.userNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ملاحظات: ${log.userNotes}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MutedSteelText
                        )
                    }
                }
            }
        }

        // 6. Footer Brand Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                BodyCompleteBrandHeader(compact = true)
            }
        }
    }
}

@Composable
private fun SummaryStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF081223))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = NeonCyanAccent,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MutedSteelText
        )
    }
}
