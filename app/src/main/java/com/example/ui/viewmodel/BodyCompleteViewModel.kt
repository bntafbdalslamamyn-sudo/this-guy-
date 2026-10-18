package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BudgetMealSuggestion
import com.example.data.model.DailyStatsItem
import com.example.data.model.DefaultWorkoutSchedule
import com.example.data.model.ExerciseItem
import com.example.data.model.WorkoutDaySpec
import com.example.data.model.WorkoutLogItem
import com.example.data.repository.BodyCompleteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class BodyCompleteViewModel(
    private val repository: BodyCompleteRepository,
    val currentUserId: String
) : ViewModel() {

    val todayDateKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val _selectedDayId = MutableStateFlow("sat")
    val selectedDayId: StateFlow<String> = _selectedDayId.asStateFlow()

    private val _selectedMuscleFilter = MutableStateFlow("all")
    val selectedMuscleFilter: StateFlow<String> = _selectedMuscleFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Rest timer state
    private val _restTimerRemaining = MutableStateFlow(0)
    val restTimerRemaining: StateFlow<Int> = _restTimerRemaining.asStateFlow()

    private val _restTimerTotal = MutableStateFlow(90)
    val restTimerTotal: StateFlow<Int> = _restTimerTotal.asStateFlow()

    private val _isRestTimerRunning = MutableStateFlow(false)
    val isRestTimerRunning: StateFlow<Boolean> = _isRestTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    val exercisesState: StateFlow<UiState<List<ExerciseItem>>> = repository
        .observeExercises(currentUserId)
        .map<List<ExerciseItem>, UiState<List<ExerciseItem>>> { list ->
            val effective = if (list.isEmpty()) {
                DefaultWorkoutSchedule.defaultExercisesForUser(currentUserId)
            } else {
                list
            }
            UiState.Success(effective)
        }
        .catch { e ->
            Log.w(TAG, "Error observing exercises", e)
            emit(UiState.Error(e.localizedMessage ?: "تعذر تحميل جدول التمارين"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(DefaultWorkoutSchedule.defaultExercisesForUser(currentUserId))
        )

    val workoutLogsState: StateFlow<UiState<List<WorkoutLogItem>>> = repository
        .observeWorkoutLogs(currentUserId)
        .map<List<WorkoutLogItem>, UiState<List<WorkoutLogItem>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing workout logs", e)
            emit(UiState.Error(e.localizedMessage ?: "تعذر تحميل سجل التمارين"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(emptyList())
        )

    val dailyStatsState: StateFlow<UiState<List<DailyStatsItem>>> = repository
        .observeDailyStats(currentUserId)
        .map<List<DailyStatsItem>, UiState<List<DailyStatsItem>>> { UiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing daily stats", e)
            emit(UiState.Error(e.localizedMessage ?: "تعذر تحميل بيانات التغذية والقياسات"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(emptyList())
        )

    init {
        viewModelScope.launch {
            try {
                repository.seedDefaultScheduleIfEmpty(currentUserId)
            } catch (e: Exception) {
                Log.w(TAG, "Initial schedule seed skipped or failed", e)
            }
        }
    }

    fun selectDay(dayId: String) {
        _selectedDayId.value = dayId
    }

    fun selectMuscleFilter(muscleKey: String) {
        _selectedMuscleFilter.value = muscleKey
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun currentTodayStats(): DailyStatsItem {
        val list = (dailyStatsState.value as? UiState.Success)?.data.orEmpty()
        return list.firstOrNull { it.dateKey == todayDateKey }
            ?: list.firstOrNull()
            ?: DailyStatsItem(
                id = todayDateKey,
                userId = currentUserId,
                dateKey = todayDateKey
            )
    }

    fun getDaySpec(dayId: String): WorkoutDaySpec {
        return DefaultWorkoutSchedule.weeklyDays.firstOrNull { it.dayId == dayId }
            ?: DefaultWorkoutSchedule.weeklyDays.first()
    }

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        val duration = seconds.coerceIn(10, 600)
        _restTimerTotal.value = duration
        _restTimerRemaining.value = duration
        _isRestTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_restTimerRemaining.value > 0) {
                delay(1000L)
                _restTimerRemaining.value = (_restTimerRemaining.value - 1).coerceAtLeast(0)
            }
            _isRestTimerRunning.value = false
            _statusBannerMessage.value = "انتهت فترة الراحة! استعد للمجموعة التالية بقوة"
        }
    }

    fun stopRestTimer() {
        timerJob?.cancel()
        _isRestTimerRunning.value = false
        _restTimerRemaining.value = 0
    }

    fun saveExerciseEdits(exercise: ExerciseItem) {
        viewModelScope.launch {
            val result = repository.saveOrUpdateExercise(
                exercise.copy(userId = currentUserId, isCustomModified = true)
            )
            if (result.isSuccess) {
                _statusBannerMessage.value = "تم حفظ تعديلات التمرين بنجاح"
            } else {
                _statusBannerMessage.value = "تعذر حفظ التمرين: ${result.exceptionOrNull()?.localizedMessage ?: ""}"
            }
        }
    }

    fun resetScheduleToOriginal() {
        viewModelScope.launch {
            try {
                repository.resetToOriginalSchedule(currentUserId)
                _statusBannerMessage.value = "تمت استعادة جدول التمارين الأسبوعي الأصلي بنجاح"
            } catch (e: Exception) {
                _statusBannerMessage.value = "تعذر استعادة الجدول الأصلي: ${e.localizedMessage ?: ""}"
            }
        }
    }

    fun completeAndSaveWorkout(
        exercise: ExerciseItem,
        completedSets: Int,
        weightKg: Double,
        repsCompleted: String,
        durationMinutes: Int,
        userNotes: String,
        onSaved: () -> Unit = {}
    ) {
        val daySpec = getDaySpec(exercise.dayId)
        val log = WorkoutLogItem(
            userId = currentUserId,
            dayId = exercise.dayId,
            dayTitleAr = "${daySpec.dayNameAr} — ${daySpec.titleAr}",
            exerciseId = exercise.id,
            exerciseNameAr = exercise.nameAr,
            muscleKey = exercise.muscleKey,
            completedSets = completedSets.coerceIn(1, 20).toLong(),
            targetSets = exercise.setsCount.coerceIn(1L, 20L),
            weightKg = weightKg.coerceIn(0.0, 1000.0),
            repsCompleted = repsCompleted.ifBlank { exercise.repsRange }.take(60),
            durationMinutes = durationMinutes.coerceIn(1, 360).toLong(),
            userNotes = userNotes.take(500),
            dateKey = todayDateKey
        )
        viewModelScope.launch {
            val res = repository.recordWorkoutSession(log)
            if (res.isSuccess) {
                _statusBannerMessage.value = "تم حفظ نتائج التمرين وتحديث تقدمك الأسبوعي!"
                onSaved()
            } else {
                _statusBannerMessage.value = "تعذر حفظ التمرين: ${res.exceptionOrNull()?.localizedMessage ?: ""}"
            }
        }
    }

    fun updateWaterCups(delta: Int) {
        val current = currentTodayStats()
        val updatedCups = (current.waterCups + delta).coerceIn(0L, 30L)
        viewModelScope.launch {
            repository.saveOrUpdateDailyStats(current.copy(dateKey = todayDateKey, waterCups = updatedCups))
        }
    }

    fun updateBodyMetrics(
        bodyWeightKg: Double,
        chestCm: Double,
        armCm: Double,
        waistCm: Double,
        thighCm: Double,
        stepsCount: Long,
        sleepHours: Double
    ) {
        val current = currentTodayStats()
        viewModelScope.launch {
            val res = repository.saveOrUpdateDailyStats(
                current.copy(
                    dateKey = todayDateKey,
                    bodyWeightKg = bodyWeightKg.coerceIn(20.0, 350.0),
                    chestCm = chestCm.coerceIn(0.0, 250.0),
                    armCm = armCm.coerceIn(0.0, 150.0),
                    waistCm = waistCm.coerceIn(0.0, 250.0),
                    thighCm = thighCm.coerceIn(0.0, 200.0),
                    stepsCount = stepsCount.coerceIn(0L, 100000L),
                    sleepHours = sleepHours.coerceIn(0.0, 24.0)
                )
            )
            if (res.isSuccess) {
                _statusBannerMessage.value = "تم حفظ الوزن والقياسات الجسدية بنجاح"
            }
        }
    }

    fun updateNutritionGoalAndTargets(
        goal: String,
        bodyWeightKg: Double,
        activityMultiplier: Double
    ) {
        val current = currentTodayStats()
        val maintenance = (bodyWeightKg * 24.0 * activityMultiplier).toInt()
        val targetCals = when (goal) {
            "bulk" -> (maintenance + 350).coerceIn(1200, 7000)
            "cut" -> (maintenance - 450).coerceIn(1000, 6000)
            else -> maintenance.coerceIn(1200, 6500)
        }
        viewModelScope.launch {
            repository.saveOrUpdateDailyStats(
                current.copy(
                    dateKey = todayDateKey,
                    bodyWeightKg = bodyWeightKg.coerceIn(20.0, 350.0),
                    nutritionGoal = goal,
                    targetCalories = targetCals.toLong()
                )
            )
            _statusBannerMessage.value = "تم تحديث هدف التغذية والسعرات اليومية المقدرة"
        }
    }

    fun logMealEntry(
        mealTitle: String,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int
    ) {
        val current = currentTodayStats()
        val newEntry = "$mealTitle ($calories سعرة | ${proteinG}g بروتين)"
        val updatedSummary = if (current.mealsLogSummary.isBlank()) {
            newEntry
        } else {
            "${current.mealsLogSummary}\n• $newEntry"
        }.take(2000)

        viewModelScope.launch {
            val res = repository.saveOrUpdateDailyStats(
                current.copy(
                    dateKey = todayDateKey,
                    consumedCalories = (current.consumedCalories + calories).coerceIn(0L, 15000L),
                    consumedProteinG = (current.consumedProteinG + proteinG).coerceIn(0L, 1000L),
                    consumedCarbsG = (current.consumedCarbsG + carbsG).coerceIn(0L, 2000L),
                    consumedFatG = (current.consumedFatG + fatG).coerceIn(0L, 1000L),
                    mealsLogSummary = updatedSummary
                )
            )
            if (res.isSuccess) {
                _statusBannerMessage.value = "تمت إضافة الوجبة إلى سجل التغذية اليومي"
            }
        }
    }

    fun logBudgetMeal(meal: BudgetMealSuggestion) {
        logMealEntry(
            mealTitle = meal.titleAr,
            calories = meal.calories,
            proteinG = meal.proteinG,
            carbsG = meal.carbsG,
            fatG = meal.fatG
        )
    }

    private companion object {
        const val TAG = "BodyCompleteVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
