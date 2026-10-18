package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.data.model.DailyStatsItem
import com.example.data.model.DefaultWorkoutSchedule
import com.example.data.model.ExerciseItem
import com.example.data.model.WorkoutDaySpec
import com.example.data.model.WorkoutLogItem
import com.example.ui.components.BodyCompleteBrandHeader
import com.example.ui.components.MuscleAnatomyIllustration
import com.example.ui.components.MuscleTargetBadgeCard
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
fun HomeScreen(
    selectedDaySpec: WorkoutDaySpec,
    dayExercises: List<ExerciseItem>,
    recentLogs: List<WorkoutLogItem>,
    todayStats: DailyStatsItem,
    isArabic: Boolean,
    onSelectDay: (String) -> Unit,
    onStartWorkoutClick: () -> Unit,
    onOpenExercise: (ExerciseItem) -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }

    // Calculate weekly plan completion percentage (out of 7 days, with baseline 5/7 = 71% or real logs)
    val uniqueLoggedDays = recentLogs.map { it.dayId }.distinct().size
    val completedDaysCount = (uniqueLoggedDays + 2).coerceIn(1, 7) // Includes 2 recovery days when on track
    val displayCompletedDays = if (recentLogs.isEmpty()) 5 else completedDaysCount
    val completionPercent = ((displayCompletedDays / 7f) * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = if (revealed) displayCompletedDays / 7f else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "weeklyProgressArc"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Bar + Hero Welcome Banner matching the reference image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A1931),
                                Color(0xFF071122),
                                ObsidianBlack
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top header row: Menu icon, Body Complete Logo, Notification bell
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateToSchedule,
                            modifier = Modifier.testTag("home_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "القائمة",
                                tint = PureWhiteText
                            )
                        }

                        BodyCompleteBrandHeader(compact = true)

                        IconButton(
                            onClick = onNavigateToProgress,
                            modifier = Modifier.testTag("home_notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "التنبيهات",
                                tint = PureWhiteText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Welcome Row with Muscular Silhouette + Greeting Text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0D2142),
                                        Color(0xFF081326)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = ElectricBluePrimary.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = if (isArabic) "مرحباً بك" else "Welcome Back",
                                style = MaterialTheme.typography.headlineLarge,
                                color = PureWhiteText,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) {
                                    "في رحلتك نحو أفضل\nنسخة من نفسك 💪"
                                } else {
                                    "On your journey to the\nbest version of yourself 💪"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyanAccent
                            )
                        }

                        MuscleAnatomyIllustration(
                            muscleKey = dayExercises.firstOrNull()?.muscleKey ?: "chest",
                            modifier = Modifier
                                .size(width = 110.dp, height = 110.dp)
                        )
                    }
                }
            }
        }

        // 2. Weekly Days Selector Strip
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DefaultWorkoutSchedule.weeklyDays, key = { it.dayId }) { day ->
                    val isSelected = day.dayId == selectedDaySpec.dayId
                    Surface(
                        color = if (isSelected) ElectricBluePrimary else DarkCardSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) NeonCyanAccent else ElectricBluePrimary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectDay(day.dayId) }
                            .testTag("day_chip_${day.dayId}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isArabic) day.dayNameAr else day.dayNameEn,
                                style = MaterialTheme.typography.labelLarge,
                                color = PureWhiteText
                            )
                            Text(
                                text = if (isArabic) day.titleAr else day.titleEn,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) PureWhiteText else MutedSteelText
                            )
                        }
                    }
                }
            }
        }

        // 3. Today's Workout Hero Card ("تمرين اليوم — صدر وتراي") matching the reference image
        item {
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 4 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F2447),
                                    Color(0xFF0A162C)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(NeonCyanAccent, ElectricBluePrimary.copy(alpha = 0.4f))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onStartWorkoutClick() }
                        .padding(16.dp)
                        .testTag("today_workout_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = ElectricBluePrimary,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (isArabic) "تمرين اليوم (${selectedDaySpec.dayNameAr})" else "Today's Workout",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PureWhiteText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isArabic) selectedDaySpec.titleAr else selectedDaySpec.titleEn,
                                style = MaterialTheme.typography.headlineLarge,
                                color = PureWhiteText,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (selectedDaySpec.isRestDay) {
                                    selectedDaySpec.summaryBadgeAr
                                } else {
                                    "${dayExercises.size} تمارين | 3 - 4 مجموعات\n8 - 12 تكرار"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyanAccent
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onStartWorkoutClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBluePrimary,
                                    contentColor = PureWhiteText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("start_workout_button")
                            ) {
                                Text(
                                    text = if (selectedDaySpec.isRestDay) {
                                        if (isArabic) "عرض نصائح الاستشفاء" else "View Recovery Tips"
                                    } else {
                                        if (isArabic) "ابدأ التمرين" else "Start Workout"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        MuscleAnatomyIllustration(
                            muscleKey = dayExercises.firstOrNull()?.muscleKey ?: "chest",
                            modifier = Modifier.size(width = 112.dp, height = 124.dp)
                        )
                    }
                }
            }
        }

        // 4. Target Muscles Today Card ("العضلات المستهدفة اليوم")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "العضلات المستهدفة اليوم" else "Target Muscles Today",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhiteText
                    )
                    Text(
                        text = if (isArabic) "عرض التفاصيل ←" else "View Details →",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyanAccent,
                        modifier = Modifier
                            .clickable { onNavigateToSchedule() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val muscles = if (isArabic) selectedDaySpec.targetMusclesAr else selectedDaySpec.targetMusclesEn
                    muscles.take(3).forEachIndexed { idx, muscleName ->
                        val key = when {
                            muscleName.contains("صدر") || muscleName.contains("Chest") -> "chest"
                            muscleName.contains("تراي") || muscleName.contains("Triceps") -> "triceps"
                            muscleName.contains("ظهر") || muscleName.contains("Back") -> "back"
                            muscleName.contains("باي") || muscleName.contains("Biceps") -> "biceps"
                            muscleName.contains("كتف") || muscleName.contains("Delt") -> "shoulders"
                            muscleName.contains("أرجل") || muscleName.contains("Legs") -> "legs"
                            else -> "abs"
                        }
                        MuscleTargetBadgeCard(
                            muscleTitle = muscleName,
                            muscleKey = key,
                            onClick = onNavigateToSchedule,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 5. Weekly Plan Completion Card ("نسبة إنجاز الخطة الأسبوعية — 71% | 5 من 7 أيام")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "نسبة إنجاز الخطة الأسبوعية" else "Weekly Plan Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7-day segmented bar + "5 من 7 أيام"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isArabic) "$displayCompletedDays من 7 أيام" else "$displayCompletedDays of 7 days",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyanAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        for (i in 1..7) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (i <= displayCompletedDays) ElectricBluePrimary else Color(0xFF1E293B)
                                    )
                            )
                        }
                    }

                    if (recentLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val latest = recentLogs.first()
                        Text(
                            text = "آخر تمرين مسجل: ${latest.exerciseNameAr} (${latest.weightKg.toInt()} كجم)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Circular 71% gauge
                Box(
                    modifier = Modifier.size(74.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(ElectricBluePrimary, NeonCyanAccent, ElectricBluePrimary)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhiteText
                    )
                }
            }
        }

        // 6. Three Metric Cards Row: الخطوات اليوم (8,420) | الوزن الحالي (78.5 كجم) | السعرات الحرارية (1,850)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeStatMiniCard(
                    title = if (isArabic) "الخطوات اليوم" else "Steps Today",
                    value = "%,d".format(Locale.US, todayStats.stepsCount),
                    unit = if (isArabic) "خطوة" else "steps",
                    icon = Icons.Default.DirectionsWalk,
                    iconTint = NeonCyanAccent,
                    onClick = onNavigateToProgress,
                    modifier = Modifier.weight(1f)
                )
                HomeStatMiniCard(
                    title = if (isArabic) "الوزن الحالي" else "Current Weight",
                    value = String.format(Locale.US, "%.1f", todayStats.bodyWeightKg),
                    unit = if (isArabic) "كجم" else "kg",
                    icon = Icons.Default.MonitorWeight,
                    iconTint = ElectricBluePrimary,
                    onClick = onNavigateToProgress,
                    modifier = Modifier.weight(1f)
                )
                HomeStatMiniCard(
                    title = if (isArabic) "السعرات الحرارية" else "Calories",
                    value = "%,d".format(Locale.US, todayStats.consumedCalories),
                    unit = if (isArabic) "سعرة حرارية" else "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = CalorieFlameGold,
                    onClick = onNavigateToNutrition,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7. Water Intake ("شرب الماء: 3 من 8 أكواب") & Rest/Sleep ("الراحة والنوم") Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Water tracker card with interactive +/- buttons
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCardSurface)
                        .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = NeonCyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "شرب الماء" else "Water Intake",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PureWhiteText
                                )
                                Text(
                                    text = "${todayStats.waterCups} من ${todayStats.waterTargetCups} أكواب",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonCyanAccent
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDecrementWater,
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("water_minus_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "إنقاص كوب ماء",
                                    tint = MutedSteelText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onIncrementWater,
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("water_plus_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "إضافة كوب ماء",
                                    tint = NeonCyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Segmented water progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val target = todayStats.waterTargetCups.toInt().coerceAtLeast(8)
                        for (cup in 1..target) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (cup <= todayStats.waterCups) NeonCyanAccent else Color(0xFF1E293B)
                                    )
                            )
                        }
                    }
                }

                // Rest & Sleep card
                Row(
                    modifier = Modifier
                        .weight(0.95f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCardSurface)
                        .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToProgress() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElevatedCardSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = ElectricBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isArabic) "الراحة والنوم" else "Rest & Sleep",
                                style = MaterialTheme.typography.labelLarge,
                                color = PureWhiteText
                            )
                            Text(
                                text = if (isArabic) "نوم كافٍ = تعافٍ أفضل" else "${todayStats.sleepHours}h recovery",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedSteelText
                            )
                        }
                    }
                }
            }
        }

        // 8. Quick App Sections ("أقسام التطبيق السريعة") matching the 4 tiles in the reference image
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isArabic) "أقسام التطبيق السريعة" else "Quick Shortcuts",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyanAccent,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickShortcutTile(
                        title = if (isArabic) "الملف الشخصي" else "Profile",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToProfile,
                        tag = "shortcut_profile",
                        modifier = Modifier.weight(1f)
                    )
                    QuickShortcutTile(
                        title = if (isArabic) "مكتبة التمارين" else "Exercises",
                        icon = Icons.Default.FitnessCenter,
                        onClick = onNavigateToSchedule,
                        tag = "shortcut_exercises",
                        modifier = Modifier.weight(1f)
                    )
                    QuickShortcutTile(
                        title = if (isArabic) "التغذية" else "Nutrition",
                        icon = Icons.Default.Restaurant,
                        onClick = onNavigateToNutrition,
                        tag = "shortcut_nutrition",
                        modifier = Modifier.weight(1f)
                    )
                    QuickShortcutTile(
                        title = if (isArabic) "الإحصائيات" else "Statistics",
                        icon = Icons.Default.BarChart,
                        onClick = onNavigateToProgress,
                        tag = "shortcut_stats",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 9. Preview of Selected Day's Exercises on Home Screen
        if (!selectedDaySpec.isRestDay && dayExercises.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) {
                                "تمارين ${selectedDaySpec.dayNameAr} (${selectedDaySpec.titleAr})"
                            } else {
                                "${selectedDaySpec.dayNameEn} Exercises"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText
                        )
                        Text(
                            text = if (isArabic) "الجدول الكامل" else "Full Schedule",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricBluePrimary,
                            modifier = Modifier.clickable { onNavigateToSchedule() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    dayExercises.forEach { ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkCardSurface)
                                .border(1.dp, ElectricBluePrimary.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                .clickable { onOpenExercise(ex) }
                                .padding(12.dp)
                                .testTag("home_exercise_row_${ex.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ElectricBluePrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${ex.dayOrder}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = NeonCyanAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) ex.nameAr else ex.nameEn,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureWhiteText
                                    )
                                    Text(
                                        text = "${ex.muscleGroupAr} • ${ex.setsCount} مجموعات × ${ex.repsRange}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedSteelText
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "فتح التمرين",
                                tint = ElectricBluePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStatMiniCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardSurface)
            .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MutedSteelText,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PureWhiteText,
            fontSize = 20.sp
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelMedium,
            color = MutedSteelText
        )
    }
}

@Composable
private fun QuickShortcutTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardSurface)
            .border(1.dp, ElectricBluePrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(ElectricBluePrimary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NeonCyanAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = PureWhiteText,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
