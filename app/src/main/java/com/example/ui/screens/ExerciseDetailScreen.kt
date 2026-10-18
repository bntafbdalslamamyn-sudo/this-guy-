package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExerciseItem
import com.example.ui.components.MuscleAnatomyIllustration
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.ElevatedCardSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MutedSteelText
import com.example.ui.theme.NeonCyanAccent
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhiteText

@Composable
fun ExerciseDetailScreen(
    exercise: ExerciseItem,
    restTimerRemaining: Int,
    restTimerTotal: Int,
    isRestTimerRunning: Boolean,
    isArabic: Boolean,
    onBack: () -> Unit,
    onStartRestTimer: (Int) -> Unit,
    onStopRestTimer: () -> Unit,
    onFinishAndSaveWorkout: (
        completedSets: Int,
        weightKg: Double,
        repsCompleted: String,
        durationMinutes: Int,
        userNotes: String
    ) -> Unit
) {
    BackHandler { onBack() }

    val totalSets = exercise.setsCount.toInt().coerceIn(1, 10)
    val completedSetsList = remember(exercise.id) {
        mutableStateListOf<Boolean>().apply {
            repeat(totalSets) { add(false) }
        }
    }

    var weightInput by remember(exercise.id) {
        mutableStateOf(
            if (exercise.lastWeightKg > 0) exercise.lastWeightKg.toInt().toString() else "40"
        )
    }
    var repsInput by remember(exercise.id) { mutableStateOf("12, 10, 10, 8") }
    var durationInput by remember(exercise.id) { mutableStateOf("45") }
    var notesInput by remember(exercise.id) { mutableStateOf("") }
    var showSetSuccessBanner by remember { mutableStateOf(false) }

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(exercise.id) { entered = true }

    val imageZoomScale by animateFloatAsState(
        targetValue = if (entered) 1.0f else 0.88f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "exerciseImageZoom"
    )

    val timerProgress by animateFloatAsState(
        targetValue = if (restTimerTotal > 0) {
            restTimerRemaining.toFloat() / restTimerTotal.toFloat()
        } else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "restTimerProgress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("exercise_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Navigation Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DarkCardSurface)
                        .testTag("exercise_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = PureWhiteText
                    )
                }

                Text(
                    text = if (isArabic) "تفاصيل وتنفيذ التمرين" else "Exercise Execution",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhiteText
                )

                Surface(
                    color = ElectricBluePrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = exercise.muscleGroupAr,
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyanAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 2. Hero Anatomical Illustration + Original Exercise Name & Sets/Reps
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(DarkCardSurface)
                    .border(1.5.dp, ElectricBluePrimary.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MuscleAnatomyIllustration(
                    muscleKey = exercise.muscleKey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .scale(imageZoomScale)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = exercise.nameAr,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PureWhiteText
                )
                Text(
                    text = exercise.nameEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonCyanAccent
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetricBadge(
                        label = "العضلة المستهدفة",
                        value = exercise.muscleGroupAr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailMetricBadge(
                        label = "المجموعات والتكرارات",
                        value = "${exercise.setsCount} × ${exercise.repsRange}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Step-by-Step Instructions ("طريقة الأداء خطوة بخطوة")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "طريقة الأداء خطوة بخطوة",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyanAccent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = exercise.instructionsAr,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PureWhiteText
                )
                if (exercise.notesAr.isNotBlank()) {
                    Surface(
                        color = ElevatedCardSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 ملاحظات التدريب والراحة: ${exercise.notesAr}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyanAccent,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // 4. Interactive Circular Rest Timer ("مؤقت الراحة بين المجموعات")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ElevatedCardSurface)
                    .border(1.dp, NeonCyanAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonCyanAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مؤقت الراحة بين المجموعات",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText
                        )
                    }
                    Text(
                        text = "الموصى به: ${exercise.restSeconds} ثانية",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedSteelText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Animated Circular Countdown Ring
                    Box(
                        modifier = Modifier.size(108.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0xFF1E293B),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(ElectricBluePrimary, NeonCyanAccent, ElectricBluePrimary)
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * timerProgress,
                                useCenter = false,
                                style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isRestTimerRunning || restTimerRemaining > 0) {
                                    "${restTimerRemaining}s"
                                } else {
                                    "${exercise.restSeconds}s"
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PureWhiteText
                            )
                            Text(
                                text = if (isRestTimerRunning) "جاري العد" else "جاهز",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyanAccent
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(60, 90, 120).forEach { sec ->
                                OutlinedButton(
                                    onClick = { onStartRestTimer(sec) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("${sec}s", color = NeonCyanAccent)
                                }
                            }
                        }

                        if (isRestTimerRunning) {
                            Button(
                                onClick = onStopRestTimer,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("stop_rest_timer_button")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إيقاف المؤقت")
                            }
                        } else {
                            Button(
                                onClick = { onStartRestTimer(exercise.restSeconds.toInt()) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("start_rest_timer_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بدء مؤقت الراحة")
                            }
                        }
                    }
                }
            }
        }

        // 5. Working Weight, Set Completion Checklist ("زر اكتملت المجموعة"), and Notes
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تسجيل الأوزان وإنجاز المجموعات",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhiteText,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("الوزن المستخدم (كجم)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = ElectricBluePrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("weight_used_input")
                    )

                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        label = { Text("التكرارات الفعلية") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = ElectricBluePrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reps_completed_input")
                    )
                }

                // Individual sets + "اكتملت المجموعة" button
                completedSetsList.forEachIndexed { index, isDone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDone) EmeraldSuccess.copy(alpha = 0.16f) else Color(0xFF081223))
                            .border(
                                width = 1.dp,
                                color = if (isDone) EmeraldSuccess else ElectricBluePrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "المجموعة ${index + 1} من $totalSets",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhiteText
                            )
                            Text(
                                text = "الهدف: ${exercise.repsRange} • الوزن: $weightInput كجم",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSteelText
                            )
                        }

                        Button(
                            onClick = {
                                completedSetsList[index] = !isDone
                                if (!isDone) {
                                    showSetSuccessBanner = true
                                    onStartRestTimer(exercise.restSeconds.toInt())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDone) EmeraldSuccess else ElectricBluePrimary,
                                contentColor = PureWhiteText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("complete_set_button_${index + 1}")
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isDone) "مكتملة ✓" else "اكتملت المجموعة")
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showSetSuccessBanner,
                    enter = fadeIn(tween(300)) + scaleIn(tween(300))
                ) {
                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ أحسنت! تم احتساب المجموعة وبدأ مؤقت الراحة تلقائيًا.",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = { durationInput = it },
                        label = { Text("مدة التمرين (دقيقة)") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات التمرين") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("workout_notes_input")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val doneCount = completedSetsList.count { it }.let {
                            if (it == 0) totalSets else it
                        }
                        val weightVal = weightInput.toDoubleOrNull() ?: exercise.lastWeightKg
                        val durationVal = durationInput.toIntOrNull() ?: 45
                        onFinishAndSaveWorkout(
                            doneCount,
                            weightVal,
                            repsInput,
                            durationVal,
                            notesInput
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBluePrimary,
                        contentColor = PureWhiteText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("finish_workout_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إنهاء التمرين وحفظ النتائج",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailMetricBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF081223))
            .border(1.dp, ElectricBluePrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MutedSteelText
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = PureWhiteText
        )
    }
}
