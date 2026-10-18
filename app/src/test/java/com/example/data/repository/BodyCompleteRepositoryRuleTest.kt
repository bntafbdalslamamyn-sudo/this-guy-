package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.DailyStatsItem
import com.example.data.model.ExerciseItem
import com.example.data.model.WorkoutLogItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BodyCompleteRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveExercise_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BodyCompleteRepository(firestore)
        val exerciseId = "ex_${UUID.randomUUID().toString().replace("-", "")}"

        val item = ExerciseItem(
            id = exerciseId,
            userId = aliceUid,
            dayId = "sat",
            dayOrder = 1L,
            nameAr = "ضغط بار مستوي (Barbell Bench Press)",
            nameEn = "Barbell Bench Press",
            muscleGroupAr = "الصدر",
            muscleGroupEn = "Chest",
            muscleKey = "chest",
            setsCount = 4L,
            repsRange = "8 - 12 تكرار",
            restSeconds = 90L,
            instructionsAr = "خطوات الأداء التفصيلية",
            instructionsEn = "Step by step instructions",
            notesAr = "ملاحظات التدريب",
            lastWeightKg = 65.0,
            isCustomModified = false
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveOrUpdateExercise(item)
        }
        assertTrue(saveResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeExercises(aliceUid).first { list -> list.any { it.id == exerciseId } }
        }
        assertTrue(emitted.any { it.id == exerciseId && it.lastWeightKg == 65.0 })
    }

    @Test
    fun recordWorkoutAndDailyStats_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BodyCompleteRepository(firestore)
        val logId = "log_${UUID.randomUUID().toString().replace("-", "")}"
        val dateKey = "2026-10-17"

        val logResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordWorkoutSession(
                WorkoutLogItem(
                    id = logId,
                    userId = aliceUid,
                    dayId = "sat",
                    dayTitleAr = "السبت — صدر وتراي",
                    exerciseId = "sat_chest_1",
                    exerciseNameAr = "ضغط بار مستوي (Barbell Bench Press)",
                    muscleKey = "chest",
                    completedSets = 4L,
                    targetSets = 4L,
                    weightKg = 70.0,
                    repsCompleted = "12, 10, 10, 8",
                    durationMinutes = 40L,
                    userNotes = "تمرين ممتاز",
                    dateKey = dateKey
                ),
                updateExerciseLastWeight = false
            )
        }
        assertTrue(logResult.isSuccess)

        val statsResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveOrUpdateDailyStats(
                DailyStatsItem(
                    id = dateKey,
                    userId = aliceUid,
                    dateKey = dateKey,
                    waterCups = 6L,
                    waterTargetCups = 8L,
                    bodyWeightKg = 78.5,
                    stepsCount = 8420L,
                    sleepHours = 8.0,
                    nutritionGoal = "bulk",
                    targetCalories = 2600L,
                    consumedCalories = 1850L
                )
            )
        }
        assertTrue(statsResult.isSuccess)
    }

    @Test
    fun getExerciseById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = BodyCompleteRepository(firestore)
        val exerciseId = "ex_${UUID.randomUUID().toString().replace("-", "")}"

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveOrUpdateExercise(
                ExerciseItem(
                    id = exerciseId,
                    userId = aliceUid,
                    dayId = "sat",
                    dayOrder = 1L,
                    nameAr = "ضغط بار مستوي",
                    nameEn = "Barbell Bench Press",
                    muscleGroupAr = "الصدر",
                    muscleGroupEn = "Chest",
                    muscleKey = "chest",
                    setsCount = 4L,
                    repsRange = "8 - 12 تكرار",
                    restSeconds = 90L,
                    instructionsAr = "خطوات",
                    instructionsEn = "Steps",
                    notesAr = "ملاحظات",
                    lastWeightKg = 60.0
                )
            )
        }
        assertTrue(saveResult.isSuccess)

        signInTestUser(BOB_EMAIL)
        val bobRepo = BodyCompleteRepository(firestore)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getExerciseById(aliceUid, exerciseId)
        }
        assertTrue(crossReadResult.isFailure)
        val ex = crossReadResult.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeExercises_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = BodyCompleteRepository(firestore)
        var caught: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeExercises("unauth_uid_123").first()
            }
        } catch (e: FirebaseFirestoreException) {
            caught = e
        }
        assertNotNull(caught)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, caught?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@bodycomplete.test"
        const val BOB_EMAIL = "bob@bodycomplete.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
