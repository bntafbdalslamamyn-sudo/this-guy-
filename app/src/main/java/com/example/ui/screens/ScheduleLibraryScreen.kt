package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DefaultWorkoutSchedule
import com.example.data.model.ExerciseItem
import com.example.data.model.WorkoutDaySpec
import com.example.ui.components.MuscleAnatomyIllustration
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.ElevatedCardSurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MutedSteelText
import com.example.ui.theme.NeonCyanAccent
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhiteText

private data class MuscleFilterChip(val key: String, val labelAr: String, val labelEn: String)

private val muscleFilters = listOf(
    MuscleFilterChip("all", "جميع العضلات", "All Muscles"),
    MuscleFilterChip("chest", "الصدر", "Chest"),
    MuscleFilterChip("triceps", "الترايسبس", "Triceps"),
    MuscleFilterChip("back", "الظهر", "Back"),
    MuscleFilterChip("biceps", "البايسبس", "Biceps"),
    MuscleFilterChip("shoulders", "الأكتاف", "Shoulders"),
    MuscleFilterChip("legs", "الأرجل", "Legs"),
    MuscleFilterChip("abs", "البطن", "Abs")
)

@Composable
fun ScheduleLibraryScreen(
    selectedDayId: String,
    selectedMuscleFilter: String,
    searchQuery: String,
    allExercises: List<ExerciseItem>,
    isArabic: Boolean,
    onSelectDay: (String) -> Unit,
    onSelectMuscleFilter: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenExercise: (ExerciseItem) -> Unit,
    onSaveExerciseEdits: (ExerciseItem) -> Unit,
    onResetSchedule: () -> Unit
) {
    val selectedDaySpec = DefaultWorkoutSchedule.weeklyDays.firstOrNull { it.dayId == selectedDayId }
        ?: DefaultWorkoutSchedule.weeklyDays.first()

    var editingExercise by remember { mutableStateOf<ExerciseItem?>(null) }

    val filteredExercises = remember(allExercises, selectedDayId, selectedMuscleFilter, searchQuery) {
        allExercises.filter { ex ->
            val matchesDay = if (searchQuery.isNotBlank() || selectedMuscleFilter != "all") {
                true
            } else {
                ex.dayId == selectedDayId
            }
            val matchesMuscle = selectedMuscleFilter == "all" || ex.muscleKey == selectedMuscleFilter
            val matchesSearch = searchQuery.isBlank() ||
                ex.nameAr.contains(searchQuery, ignoreCase = true) ||
                ex.nameEn.contains(searchQuery, ignoreCase = true) ||
                ex.muscleGroupAr.contains(searchQuery, ignoreCase = true)
            matchesDay && matchesMuscle && matchesSearch
        }.sortedWith(compareBy<ExerciseItem> { it.dayId }.thenBy { it.dayOrder })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("schedule_library_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Restore Original Schedule Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "جدول التمارين الأسبوعي" else "Weekly Workout Schedule",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhiteText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) {
                            "Body Complete – كامل الأجسام (بيانات فعلية قابلة للتعديل)"
                        } else {
                            "Body Complete Official Structured Split"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonCyanAccent
                    )
                }

                OutlinedButton(
                    onClick = onResetSchedule,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("reset_schedule_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = NeonCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "استعادة الأصلي" else "Reset",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyanAccent
                    )
                }
            }
        }

        // 2. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = if (isArabic) "ابحث عن تمرين أو عضلة مستهدفة..." else "Search exercise or muscle...",
                        color = MutedSteelText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NeonCyanAccent
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = ElectricBluePrimary.copy(alpha = 0.3f),
                    focusedContainerColor = DarkCardSurface,
                    unfocusedContainerColor = DarkCardSurface,
                    focusedTextColor = PureWhiteText,
                    unfocusedTextColor = PureWhiteText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_exercise_input")
            )
        }

        // 3. Muscle Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(muscleFilters, key = { it.key }) { chip ->
                    val selected = chip.key == selectedMuscleFilter
                    Surface(
                        color = if (selected) ElectricBluePrimary else DarkCardSurface,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(
                                width = 1.dp,
                                color = if (selected) NeonCyanAccent else ElectricBluePrimary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onSelectMuscleFilter(chip.key) }
                            .testTag("muscle_filter_${chip.key}")
                    ) {
                        Text(
                            text = if (isArabic) chip.labelAr else chip.labelEn,
                            style = MaterialTheme.typography.labelLarge,
                            color = PureWhiteText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 4. Weekly Days Cards with Animated Elevation & Glowing Electric Blue Border
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DefaultWorkoutSchedule.weeklyDays, key = { it.dayId }) { day ->
                    val isSelected = day.dayId == selectedDayId && selectedMuscleFilter == "all" && searchQuery.isBlank()
                    val elevation by animateDpAsState(
                        targetValue = if (isSelected) 10.dp else 2.dp,
                        animationSpec = tween(250),
                        label = "dayElevation"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) NeonCyanAccent else ElectricBluePrimary.copy(alpha = 0.25f),
                        animationSpec = tween(250),
                        label = "dayBorder"
                    )

                    Column(
                        modifier = Modifier
                            .width(138.dp)
                            .shadow(elevation, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) {
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF12305E), Color(0xFF0B1C38))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(DarkCardSurface, DarkCardSurface)
                                    )
                                }
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                onSelectMuscleFilter("all")
                                onSearchQueryChange("")
                                onSelectDay(day.dayId)
                            }
                            .padding(12.dp)
                            .testTag("schedule_day_card_${day.dayId}")
                    ) {
                        Text(
                            text = if (isArabic) day.dayNameAr else day.dayNameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonCyanAccent else PureWhiteText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) day.titleAr else day.titleEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhiteText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (day.isRestDay) "استشفاء" else "تدريب عضلي",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (day.isRestDay) EmeraldSuccess else MutedSteelText
                        )
                    }
                }
            }
        }

        // 5. Selected Day Summary Banner
        item {
            DayHeaderSummaryCard(daySpec = selectedDaySpec, isArabic = isArabic)
        }

        // 6. Rest & Recovery View (Tuesday & Friday) OR Structured Exercise Cards
        if (selectedDaySpec.isRestDay && selectedMuscleFilter == "all" && searchQuery.isBlank()) {
            item {
                RestAndRecoveryDayPanel(daySpec = selectedDaySpec, isArabic = isArabic)
            }
        } else {
            items(filteredExercises, key = { it.id }) { exercise ->
                val daySpec = DefaultWorkoutSchedule.weeklyDays.firstOrNull { it.dayId == exercise.dayId }
                StructuredExerciseTableCard(
                    exercise = exercise,
                    dayTitle = if (daySpec != null) "${daySpec.dayNameAr} — ${daySpec.titleAr}" else exercise.dayId,
                    isArabic = isArabic,
                    onStartExercise = { onOpenExercise(exercise) },
                    onEditExercise = { editingExercise = exercise }
                )
            }
        }
    }

    if (editingExercise != null) {
        EditExerciseDialog(
            exercise = editingExercise!!,
            onDismiss = { editingExercise = null },
            onSave = { updated ->
                onSaveExerciseEdits(updated)
                editingExercise = null
            }
        )
    }
}

@Composable
private fun DayHeaderSummaryCard(
    daySpec: WorkoutDaySpec,
    isArabic: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ElevatedCardSurface)
            .border(1.dp, ElectricBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) {
                    "${daySpec.dayNameAr} — ${daySpec.titleAr}"
                } else {
                    "${daySpec.dayNameEn} — ${daySpec.titleEn}"
                },
                style = MaterialTheme.typography.titleLarge,
                color = PureWhiteText
            )
            Surface(
                color = if (daySpec.isRestDay) EmeraldSuccess.copy(alpha = 0.2f) else ElectricBluePrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = daySpec.summaryBadgeAr,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (daySpec.isRestDay) EmeraldSuccess else NeonCyanAccent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "العضلات المستهدفة: ${daySpec.targetMusclesAr.joinToString(" ، ")}",
            style = MaterialTheme.typography.bodyMedium,
            color = NeonCyanAccent
        )
    }
}

@Composable
private fun RestAndRecoveryDayPanel(
    daySpec: WorkoutDaySpec,
    isArabic: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCardSurface)
            .border(1.5.dp, EmeraldSuccess.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isArabic) "يوم مخصص للراحة والاستشفاء العضلي" else "Dedicated Rest & Muscle Recovery Day",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhiteText
                )
                Text(
                    text = "العضلات تنمو وتزداد قوة أثناء الراحة والنوم والتغذية المتوازنة وليس أثناء التمرين فقط.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSteelText
                )
            }
        }

        daySpec.recoveryTipsAr.forEach { tip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF091426))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PureWhiteText
                )
            }
        }
    }
}

@Composable
private fun StructuredExerciseTableCard(
    exercise: ExerciseItem,
    dayTitle: String,
    isArabic: Boolean,
    onStartExercise: () -> Unit,
    onEditExercise: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCardSurface)
            .border(1.dp, ElectricBluePrimary.copy(alpha = 0.32f), RoundedCornerShape(20.dp))
            .clickable { onStartExercise() }
            .padding(16.dp)
            .testTag("schedule_exercise_card_${exercise.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                MuscleAnatomyIllustration(
                    muscleKey = exercise.muscleKey,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$dayTitle • تمرين #${exercise.dayOrder}",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyanAccent
                    )
                    Text(
                        text = if (isArabic) exercise.nameAr else exercise.nameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhiteText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "العضلة المستهدفة: ${exercise.muscleGroupAr}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSteelText
                    )
                }
            }

            IconButton(
                onClick = onEditExercise,
                modifier = Modifier.testTag("edit_exercise_btn_${exercise.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تعديل التمرين",
                    tint = NeonCyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Structured table metadata pills: Sets & Reps | Rest | Working Weight
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExerciseMetaPill(
                icon = Icons.Default.FitnessCenter,
                text = "${exercise.setsCount} مجموعات × ${exercise.repsRange}",
                modifier = Modifier.weight(1.2f)
            )
            ExerciseMetaPill(
                icon = Icons.Default.Timer,
                text = "راحة ${exercise.restSeconds} ث",
                modifier = Modifier.weight(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Instructions & Notes preview
        Text(
            text = "طريقة الأداء: ${exercise.instructionsAr.replace("\n", " • ")}",
            style = MaterialTheme.typography.bodyMedium,
            color = PureWhiteText.copy(alpha = 0.9f),
            maxLines = 2
        )
        if (exercise.notesAr.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ملاحظات التدريب والراحة: ${exercise.notesAr}",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyanAccent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "آخر وزن مستخدم: ${exercise.lastWeightKg.toInt()} كجم",
                style = MaterialTheme.typography.labelLarge,
                color = EmeraldSuccess
            )
            Button(
                onClick = onStartExercise,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBluePrimary,
                    contentColor = PureWhiteText
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "بدء وتفاصيل التمرين" else "Open Exercise",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ExerciseMetaPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF081223))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElectricBluePrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = PureWhiteText,
            maxLines = 1
        )
    }
}

@Composable
private fun EditExerciseDialog(
    exercise: ExerciseItem,
    onDismiss: () -> Unit,
    onSave: (ExerciseItem) -> Unit
) {
    var nameAr by remember { mutableStateOf(exercise.nameAr) }
    var muscleGroupAr by remember { mutableStateOf(exercise.muscleGroupAr) }
    var setsStr by remember { mutableStateOf(exercise.setsCount.toString()) }
    var repsRange by remember { mutableStateOf(exercise.repsRange) }
    var restStr by remember { mutableStateOf(exercise.restSeconds.toString()) }
    var instructionsAr by remember { mutableStateOf(exercise.instructionsAr) }
    var notesAr by remember { mutableStateOf(exercise.notesAr) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardSurface,
        titleContentColor = PureWhiteText,
        textContentColor = PureWhiteText,
        title = {
            Text(
                text = "مراجعة وتعديل بيانات التمرين",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("اسم التمرين") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = muscleGroupAr,
                    onValueChange = { muscleGroupAr = it },
                    label = { Text("العضلة المستهدفة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setsStr,
                        onValueChange = { setsStr = it },
                        label = { Text("المجموعات") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsRange,
                        onValueChange = { repsRange = it },
                        label = { Text("التكرارات") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = restStr,
                        onValueChange = { restStr = it },
                        label = { Text("الراحة (ث)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = instructionsAr,
                    onValueChange = { instructionsAr = it },
                    label = { Text("طريقة الأداء والتفصيل") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesAr,
                    onValueChange = { notesAr = it },
                    label = { Text("ملاحظات التدريب والراحة") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        exercise.copy(
                            nameAr = nameAr.ifBlank { exercise.nameAr },
                            muscleGroupAr = muscleGroupAr.ifBlank { exercise.muscleGroupAr },
                            setsCount = setsStr.toLongOrNull()?.coerceIn(1L, 15L) ?: exercise.setsCount,
                            repsRange = repsRange.ifBlank { exercise.repsRange },
                            restSeconds = restStr.toLongOrNull()?.coerceIn(10L, 600L) ?: exercise.restSeconds,
                            instructionsAr = instructionsAr.ifBlank { exercise.instructionsAr },
                            notesAr = notesAr
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = MutedSteelText)
            }
        }
    )
}
