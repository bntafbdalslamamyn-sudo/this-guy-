package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.DailyStatsItem
import com.example.data.model.DefaultWorkoutSchedule
import com.example.data.model.ExerciseItem
import com.example.data.repository.BodyCompleteRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.screens.ExerciseDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProgressStatsScreen
import com.example.ui.screens.ScheduleLibraryScreen
import com.example.ui.theme.BodyCompleteTheme
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.MutedSteelText
import com.example.ui.theme.NeonCyanAccent
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhiteText
import com.example.ui.viewmodel.BodyCompleteViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BodyCompleteAppRoot()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

enum class MainNavTab(
    val route: String,
    val labelAr: String,
    val labelEn: String,
    val icon: ImageVector
) {
    HOME("home", "الرئيسية", "Home", Icons.Default.Home),
    EXERCISES("exercises", "التمارين", "Workouts", Icons.Default.FitnessCenter),
    NUTRITION("nutrition", "التغذية", "Nutrition", Icons.Default.Restaurant),
    PROGRESS("progress", "التقدم", "Progress", Icons.Default.BarChart),
    MORE("more", "المزيد", "More", Icons.Default.GridView)
}

@Composable
fun BodyCompleteAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        BodyCompleteTheme(isArabicRtl = true) {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        AuthenticatedBodyCompleteApp(currentUserId = user.uid)
    }
}

@Composable
fun AuthenticatedBodyCompleteApp(currentUserId: String) {
    val viewModel: BodyCompleteViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                BodyCompleteViewModel(BodyCompleteRepository(db), currentUserId)
            }
        }
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isArabic by viewModel.isArabic.collectAsStateWithLifecycle()
    val selectedDayId by viewModel.selectedDayId.collectAsStateWithLifecycle()
    val selectedMuscleFilter by viewModel.selectedMuscleFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val exercisesState by viewModel.exercisesState.collectAsStateWithLifecycle()
    val workoutLogsState by viewModel.workoutLogsState.collectAsStateWithLifecycle()
    val dailyStatsState by viewModel.dailyStatsState.collectAsStateWithLifecycle()
    val restTimerRemaining by viewModel.restTimerRemaining.collectAsStateWithLifecycle()
    val restTimerTotal by viewModel.restTimerTotal.collectAsStateWithLifecycle()
    val isRestTimerRunning by viewModel.isRestTimerRunning.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
    var activeExerciseDetail by remember { mutableStateOf<ExerciseItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    val allExercises = (exercisesState as? UiState.Success)?.data
        ?: DefaultWorkoutSchedule.defaultExercisesForUser(currentUserId)
    val workoutLogs = (workoutLogsState as? UiState.Success)?.data.orEmpty()
    val todayStats = (dailyStatsState as? UiState.Success)?.data
        ?.firstOrNull { it.dateKey == viewModel.todayDateKey }
        ?: (dailyStatsState as? UiState.Success)?.data?.firstOrNull()
        ?: DailyStatsItem(id = viewModel.todayDateKey, userId = currentUserId, dateKey = viewModel.todayDateKey)

    val selectedDaySpec = viewModel.getDaySpec(selectedDayId)
    val selectedDayExercises = allExercises.filter { it.dayId == selectedDayId }.sortedBy { it.dayOrder }

    BodyCompleteTheme(isArabicRtl = isArabic) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBlack,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BodyCompleteBottomBar(
                    currentTab = currentTab,
                    isArabic = isArabic,
                    onSelectTab = { tab ->
                        activeExerciseDetail = null
                        currentTab = tab
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val detailExercise = activeExerciseDetail
                if (detailExercise != null) {
                    ExerciseDetailScreen(
                        exercise = detailExercise,
                        restTimerRemaining = restTimerRemaining,
                        restTimerTotal = restTimerTotal,
                        isRestTimerRunning = isRestTimerRunning,
                        isArabic = isArabic,
                        onBack = { activeExerciseDetail = null },
                        onStartRestTimer = { sec -> viewModel.startRestTimer(sec) },
                        onStopRestTimer = { viewModel.stopRestTimer() },
                        onFinishAndSaveWorkout = { sets, weight, reps, duration, notes ->
                            viewModel.completeAndSaveWorkout(
                                exercise = detailExercise,
                                completedSets = sets,
                                weightKg = weight,
                                repsCompleted = reps,
                                durationMinutes = duration,
                                userNotes = notes,
                                onSaved = { activeExerciseDetail = null }
                            )
                        }
                    )
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        when (currentTab) {
                            MainNavTab.HOME -> HomeScreen(
                                selectedDaySpec = selectedDaySpec,
                                dayExercises = selectedDayExercises,
                                recentLogs = workoutLogs,
                                todayStats = todayStats,
                                isArabic = isArabic,
                                onSelectDay = { viewModel.selectDay(it) },
                                onStartWorkoutClick = {
                                    if (selectedDaySpec.isRestDay) {
                                        currentTab = MainNavTab.EXERCISES
                                    } else {
                                        val firstEx = selectedDayExercises.firstOrNull()
                                        if (firstEx != null) {
                                            activeExerciseDetail = firstEx
                                        } else {
                                            currentTab = MainNavTab.EXERCISES
                                        }
                                    }
                                },
                                onOpenExercise = { activeExerciseDetail = it },
                                onNavigateToSchedule = { currentTab = MainNavTab.EXERCISES },
                                onNavigateToNutrition = { currentTab = MainNavTab.NUTRITION },
                                onNavigateToProgress = { currentTab = MainNavTab.PROGRESS },
                                onNavigateToProfile = { currentTab = MainNavTab.MORE },
                                onIncrementWater = { viewModel.updateWaterCups(1) },
                                onDecrementWater = { viewModel.updateWaterCups(-1) }
                            )

                            MainNavTab.EXERCISES -> ScheduleLibraryScreen(
                                selectedDayId = selectedDayId,
                                selectedMuscleFilter = selectedMuscleFilter,
                                searchQuery = searchQuery,
                                allExercises = allExercises,
                                isArabic = isArabic,
                                onSelectDay = { viewModel.selectDay(it) },
                                onSelectMuscleFilter = { viewModel.selectMuscleFilter(it) },
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onOpenExercise = { activeExerciseDetail = it },
                                onSaveExerciseEdits = { viewModel.saveExerciseEdits(it) },
                                onResetSchedule = { viewModel.resetScheduleToOriginal() }
                            )

                            MainNavTab.NUTRITION -> NutritionScreen(
                                todayStats = todayStats,
                                isArabic = isArabic,
                                onUpdateGoalAndTargets = { goal, weight, mult ->
                                    viewModel.updateNutritionGoalAndTargets(goal, weight, mult)
                                },
                                onLogCustomMeal = { title, cals, p, c, f ->
                                    viewModel.logMealEntry(title, cals, p, c, f)
                                },
                                onLogBudgetMeal = { meal -> viewModel.logBudgetMeal(meal) },
                                onIncrementWater = { viewModel.updateWaterCups(1) },
                                onDecrementWater = { viewModel.updateWaterCups(-1) }
                            )

                            MainNavTab.PROGRESS, MainNavTab.MORE -> ProgressStatsScreen(
                                exercises = allExercises,
                                workoutLogs = workoutLogs,
                                todayStats = todayStats,
                                isArabic = isArabic,
                                onSaveBodyMetrics = { w, ch, ar, wa, th, st, sl ->
                                    viewModel.updateBodyMetrics(w, ch, ar, wa, th, st, sl)
                                },
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onSignOut = {
                                    signOutUser(
                                        context = context,
                                        onSignOutComplete = {},
                                        scope = scope
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyCompleteBottomBar(
    currentTab: MainNavTab,
    isArabic: Boolean,
    onSelectTab: (MainNavTab) -> Unit
) {
    Surface(
        color = Color(0xFF070F1E),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElectricBluePrimary.copy(alpha = 0.25f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainNavTab.entries.forEach { tab ->
                val selected = tab == currentTab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("bottom_nav_${tab.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.labelAr,
                        tint = if (selected) NeonCyanAccent else MutedSteelText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isArabic) tab.labelAr else tab.labelEn,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) NeonCyanAccent else MutedSteelText
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 18.dp, height = 3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(ElectricBluePrimary)
                        )
                    }
                }
            }
        }
    }
}
