package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BudgetMealSuggestion
import com.example.data.model.DailyStatsItem
import com.example.data.model.DefaultWorkoutSchedule
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
fun NutritionScreen(
    todayStats: DailyStatsItem,
    isArabic: Boolean,
    onUpdateGoalAndTargets: (goal: String, bodyWeightKg: Double, activityMultiplier: Double) -> Unit,
    onLogCustomMeal: (title: String, calories: Int, proteinG: Int, carbsG: Int, fatG: Int) -> Unit,
    onLogBudgetMeal: (BudgetMealSuggestion) -> Unit,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit
) {
    var selectedGoal by remember(todayStats.nutritionGoal) { mutableStateOf(todayStats.nutritionGoal) }
    var weightInput by remember(todayStats.bodyWeightKg) { mutableStateOf(todayStats.bodyWeightKg.toString()) }
    var activityMultiplier by remember { mutableStateOf(1.55) }

    var customMealName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("450") }
    var customProtein by remember { mutableStateOf("35") }
    var customCarbs by remember { mutableStateOf("50") }
    var customFat by remember { mutableStateOf("12") }

    // Estimated Macro Targets based on bodyWeightKg and targetCalories
    val weightKg = weightInput.toDoubleOrNull() ?: todayStats.bodyWeightKg
    val targetProteinG = (weightKg * 2.1).toInt().coerceAtLeast(110)
    val targetFatG = (weightKg * 0.9).toInt().coerceAtLeast(45)
    val remainingCalsForCarbs = (todayStats.targetCalories.toInt() - (targetProteinG * 4 + targetFatG * 9)).coerceAtLeast(400)
    val targetCarbsG = remainingCalsForCarbs / 4

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .testTag("nutrition_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Mandatory Estimation Disclaimer
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isArabic) "التغذية وحاسبة الاحتياج اليومي" else "Nutrition & Macro Calculator",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PureWhiteText,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = ElevatedCardSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تنويه مهم: الاحتياجات الغذائية والسعرات الحرارية المحسوبة تقديرية وتختلف حسب طبيعة الشخص ومستوى نشاطه البدني وحالته الصحية.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyanAccent
                        )
                    }
                }
            }
        }

        // 2. Daily Calories & Macros Summary Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CalorieFlameGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "السعرات والماكروز اليومية",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${todayStats.consumedCalories} / ${todayStats.targetCalories} سعرة",
                        style = MaterialTheme.typography.titleMedium,
                        color = CalorieFlameGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = {
                        (todayStats.consumedCalories.toFloat() / todayStats.targetCalories.toFloat().coerceAtLeast(1f))
                            .coerceIn(0f, 1f)
                    },
                    color = ElectricBluePrimary,
                    trackColor = Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MacroProgressBox(
                        label = "البروتين",
                        consumed = todayStats.consumedProteinG.toInt(),
                        target = targetProteinG,
                        color = ElectricBluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressBox(
                        label = "الكربوهيدرات",
                        consumed = todayStats.consumedCarbsG.toInt(),
                        target = targetCarbsG,
                        color = NeonCyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressBox(
                        label = "الدهون الصحية",
                        consumed = todayStats.consumedFatG.toInt(),
                        target = targetFatG,
                        color = CalorieFlameGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Goal Selection (تضخيم / تنشيف / ثبات) & Calorie Calculator
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = NeonCyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحديد الهدف وحساب الاحتياج التقديري",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhiteText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "bulk" to "زيادة الكتلة العضلية",
                        "cut" to "خسارة الدهون (تنشيف)",
                        "maintain" to "الحفاظ على الوزن"
                    ).forEach { (key, label) ->
                        val active = selectedGoal == key
                        Surface(
                            color = if (active) ElectricBluePrimary else Color(0xFF091426),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (active) NeonCyanAccent else ElectricBluePrimary.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedGoal = key }
                                .testTag("goal_option_$key")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhiteText,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("وزن الجسم (كجم)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            onUpdateGoalAndTargets(
                                selectedGoal,
                                weightInput.toDoubleOrNull() ?: todayStats.bodyWeightKg,
                                activityMultiplier
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("calculate_macros_button")
                    ) {
                        Text("تحديث الاحتياج")
                    }
                }
            }
        }

        // 4. Interactive Water Intake Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, NeonCyanAccent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = NeonCyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "متابعة شرب الماء اليومي",
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText
                        )
                        Text(
                            text = "${todayStats.waterCups} من ${todayStats.waterTargetCups} أكواب (${todayStats.waterCups * 250} مل)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyanAccent
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrementWater) {
                        Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = MutedSteelText)
                    }
                    Text(
                        text = "${todayStats.waterCups}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PureWhiteText,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onIncrementWater) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", tint = NeonCyanAccent)
                    }
                }
            }
        }

        // 5. Log Custom Meal Form + Logged Meals Today
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "تسجيل وجبة جديدة",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhiteText,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = customMealName,
                    onValueChange = { customMealName = it },
                    label = { Text("اسم الوجبة (مثال: صدور دجاج وأرز)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meal_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customCalories,
                        onValueChange = { customCalories = it },
                        label = { Text("سعرات") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customProtein,
                        onValueChange = { customProtein = it },
                        label = { Text("بروتين (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customCarbs,
                        onValueChange = { customCarbs = it },
                        label = { Text("كارب (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customFat,
                        onValueChange = { customFat = it },
                        label = { Text("دهون (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val title = customMealName.ifBlank { "وجبة عالية البروتين" }
                        onLogCustomMeal(
                            title,
                            customCalories.toIntOrNull() ?: 450,
                            customProtein.toIntOrNull() ?: 35,
                            customCarbs.toIntOrNull() ?: 50,
                            customFat.toIntOrNull() ?: 12
                        )
                        customMealName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_custom_meal_button")
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة الوجبة إلى سجل اليوم")
                }

                if (todayStats.mealsLogSummary.isNotBlank()) {
                    Surface(
                        color = Color(0xFF091426),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "الوجبات المسجلة اليوم:",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldSuccess
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = todayStats.mealsLogSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhiteText
                            )
                        }
                    }
                }
            }
        }

        // 6. Budget-Friendly Bodybuilding Meal Suggestions ("اقتراح وجبات مناسبة للميزانية")
        item {
            Text(
                text = "اقتراح وجبات رياضية مناسبة للميزانية",
                style = MaterialTheme.typography.titleLarge,
                color = PureWhiteText,
                fontWeight = FontWeight.Bold
            )
        }

        items(DefaultWorkoutSchedule.budgetMealSuggestions, key = { it.id }) { meal ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meal.titleAr,
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhiteText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = meal.budgetLevelAr,
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldSuccess
                        )
                    }
                    Button(
                        onClick = { onLogBudgetMeal(meal) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ تسجيل")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "المكونات: ${meal.ingredientsAr}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSteelText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${meal.calories} سعرة • ${meal.proteinG}g بروتين • ${meal.carbsG}g كارب • ${meal.fatG}g دهون",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyanAccent
                )
            }
        }
    }
}

@Composable
private fun MacroProgressBox(
    label: String,
    consumed: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF081223))
            .padding(10.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MutedSteelText)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${consumed}g / ${target}g",
            style = MaterialTheme.typography.labelLarge,
            color = PureWhiteText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1E293B))
        ) {
            val ratio = (consumed.toFloat() / target.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(6.dp)
                    .background(color)
            )
        }
    }
}
