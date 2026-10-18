package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.DailyStatsItem
import com.example.data.model.DefaultWorkoutSchedule
import com.example.data.model.ExerciseItem
import com.example.data.model.WorkoutLogItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BodyCompleteRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeExercises(userId: String): Flow<List<ExerciseItem>> {
        val path = "users/$userId/exercises"
        return db.collection("users").document(userId).collection("exercises")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ExerciseItem::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedWith(compareBy<ExerciseItem> { it.dayId }.thenBy { it.dayOrder })
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeWorkoutLogs(userId: String): Flow<List<WorkoutLogItem>> {
        val path = "users/$userId/workout_logs"
        return db.collection("users").document(userId).collection("workout_logs")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    WorkoutLogItem::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeDailyStats(userId: String): Flow<List<DailyStatsItem>> {
        val path = "users/$userId/daily_stats"
        return db.collection("users").document(userId).collection("daily_stats")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    DailyStatsItem::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.dateKey }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun seedDefaultScheduleIfEmpty(userId: String) {
        val colRef = db.collection("users").document(userId).collection("exercises")
        try {
            val existing = colRef.whereEqualTo("userId", userId).limit(1).get().await()
            if (existing.isEmpty) {
                resetToOriginalSchedule(userId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, colRef.path)
            throw e
        }
    }

    suspend fun resetToOriginalSchedule(userId: String) {
        val colRef = db.collection("users").document(userId).collection("exercises")
        try {
            val defaults = DefaultWorkoutSchedule.defaultExercisesForUser(userId)
            val batch = db.batch()
            for (exercise in defaults) {
                val docRef = colRef.document(exercise.id)
                batch.set(docRef, exercise.toCreateMap(userId))
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, colRef.path)
            throw e
        }
    }

    suspend fun saveOrUpdateExercise(exercise: ExerciseItem): Result<String> {
        val uid = requireUserId()
        val docId = exercise.id.ifBlank { "ex_${System.currentTimeMillis()}" }
        val docRef = db.collection("users").document(uid).collection("exercises").document(docId)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val updates = mapOf(
                    "dayId" to exercise.dayId,
                    "dayOrder" to exercise.dayOrder,
                    "nameAr" to exercise.nameAr,
                    "nameEn" to exercise.nameEn,
                    "muscleGroupAr" to exercise.muscleGroupAr,
                    "muscleGroupEn" to exercise.muscleGroupEn,
                    "muscleKey" to exercise.muscleKey,
                    "setsCount" to exercise.setsCount,
                    "repsRange" to exercise.repsRange,
                    "restSeconds" to exercise.restSeconds,
                    "instructionsAr" to exercise.instructionsAr,
                    "instructionsEn" to exercise.instructionsEn,
                    "notesAr" to exercise.notesAr,
                    "lastWeightKg" to exercise.lastWeightKg,
                    "isCustomModified" to exercise.isCustomModified,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            } else {
                docRef.set(exercise.copy(id = docId, userId = uid).toCreateMap(uid)).await()
            }
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getExerciseById(ownerUid: String, exerciseId: String): Result<ExerciseItem?> {
        val docRef = db.collection("users").document(ownerUid).collection("exercises").document(exerciseId)
        return try {
            val snap = docRef.get().await()
            Result.success(snap.toObject(ExerciseItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordWorkoutSession(
        log: WorkoutLogItem,
        updateExerciseLastWeight: Boolean = true
    ): Result<String> {
        val uid = requireUserId()
        val logId = log.id.ifBlank { "log_${System.currentTimeMillis()}" }
        val logRef = db.collection("users").document(uid).collection("workout_logs").document(logId)
        return try {
            logRef.set(log.copy(id = logId, userId = uid).toCreateMap(uid)).await()
            if (updateExerciseLastWeight && log.exerciseId.isNotBlank() && log.weightKg > 0.0) {
                val exRef = db.collection("users").document(uid).collection("exercises").document(log.exerciseId)
                val exSnap = exRef.get().await()
                if (exSnap.exists()) {
                    exRef.update(
                        mapOf(
                            "lastWeightKg" to log.weightKg,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(logId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, logRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveOrUpdateDailyStats(stats: DailyStatsItem): Result<String> {
        val uid = requireUserId()
        val dateKey = stats.dateKey.ifBlank { "2026-10-17" }
        val docRef = db.collection("users").document(uid).collection("daily_stats").document(dateKey)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val updates = mapOf(
                    "waterCups" to stats.waterCups,
                    "waterTargetCups" to stats.waterTargetCups,
                    "bodyWeightKg" to stats.bodyWeightKg,
                    "stepsCount" to stats.stepsCount,
                    "sleepHours" to stats.sleepHours,
                    "chestCm" to stats.chestCm,
                    "armCm" to stats.armCm,
                    "waistCm" to stats.waistCm,
                    "thighCm" to stats.thighCm,
                    "nutritionGoal" to stats.nutritionGoal,
                    "targetCalories" to stats.targetCalories,
                    "consumedCalories" to stats.consumedCalories,
                    "consumedProteinG" to stats.consumedProteinG,
                    "consumedCarbsG" to stats.consumedCarbsG,
                    "consumedFatG" to stats.consumedFatG,
                    "mealsLogSummary" to stats.mealsLogSummary,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            } else {
                docRef.set(stats.copy(id = dateKey, dateKey = dateKey, userId = uid).toCreateMap(uid)).await()
            }
            Result.success(dateKey)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
