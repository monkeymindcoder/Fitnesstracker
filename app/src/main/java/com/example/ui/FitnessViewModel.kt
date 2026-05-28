package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FitnessDatabase.getDatabase(application)
    private val repository = FitnessRepository(database.fitnessDao())

    val workoutLogs: StateFlow<List<WorkoutLog>> = repository.allWorkoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs: StateFlow<List<WeightLog>> = repository.allWeightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI stats derived dynamically from active flows
    val dashboardStats = combine(workoutLogs, weightLogs) { workouts, weights ->
        val totalWorkouts = workouts.size
        val totalCardioMinutes = workouts.filter { it.workoutType == "Cardio" || it.workoutType == "Mindfulness" }
            .sumOf { it.durationMinutes ?: 0 }
        val totalStrengthMinutes = workouts.filter { it.workoutType == "Strength" }
            .sumOf { it.durationMinutes ?: 0 }
        val totalCalories = workouts.sumOf { it.caloriesBurned ?: 0 }
        
        val currentWeight = weights.firstOrNull()?.weightKg ?: 75.0f
        val startWeight = weights.lastOrNull()?.weightKg ?: currentWeight
        val weightChange = currentWeight - startWeight

        DashboardStats(
            totalWorkouts = totalWorkouts,
            totalActiveMinutes = totalCardioMinutes + totalStrengthMinutes,
            totalCaloriesBurned = totalCalories,
            currentWeightKg = currentWeight,
            weightChangeKg = weightChange,
            streakDays = calculateStreak(workouts)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun insertWorkout(
        type: String,
        name: String,
        sets: Int?,
        reps: Int?,
        weight: Float?,
        duration: Int?,
        calories: Int?,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertWorkoutLog(
                WorkoutLog(
                    workoutType = type,
                    exerciseName = name,
                    sets = sets,
                    reps = reps,
                    weightKg = weight,
                    durationMinutes = duration,
                    caloriesBurned = calories,
                    notes = notes
                )
            )
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkoutLogById(id)
        }
    }

    fun insertWeight(weight: Float, notes: String) {
        viewModelScope.launch {
            repository.insertWeightLog(
                WeightLog(
                    weightKg = weight,
                    notes = notes
                )
            )
        }
    }

    fun deleteWeight(id: Int) {
        viewModelScope.launch {
            repository.deleteWeightLogById(id)
        }
    }

    fun generateSampleData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            // Sample weight logs representing consistent healthy progress
            repository.insertWeightLog(WeightLog(weightKg = 74.2f, notes = "Feeling lighter!", timestamp = now))
            repository.insertWeightLog(WeightLog(weightKg = 74.8f, notes = "Post run weigh-in", timestamp = now - 2 * dayMs))
            repository.insertWeightLog(WeightLog(weightKg = 75.4f, notes = "Hydrating well", timestamp = now - 4 * dayMs))
            repository.insertWeightLog(WeightLog(weightKg = 76.5f, notes = "Baseline weight, ready to push!", timestamp = now - 7 * dayMs))

            // Sample workout logs representing diverse modern training types
            repository.insertWorkoutLog(WorkoutLog(
                workoutType = "Strength",
                exerciseName = "Barbell Back Squats",
                sets = 4,
                reps = 10,
                weightKg = 60f,
                durationMinutes = 45,
                caloriesBurned = 320,
                notes = "Felt strong today, focus on perfect form",
                timestamp = now - 1 * dayMs
            ))
            repository.insertWorkoutLog(WorkoutLog(
                workoutType = "Cardio",
                exerciseName = "Outdoor Run (5K)",
                durationMinutes = 28,
                caloriesBurned = 340,
                notes = "Clean outdoor pacing, cool weather",
                timestamp = now - 2 * dayMs
            ))
            repository.insertWorkoutLog(WorkoutLog(
                workoutType = "Mindfulness",
                exerciseName = "Vinyasa Flow Yoga",
                durationMinutes = 20,
                caloriesBurned = 90,
                notes = "Deep stretching and spine relief",
                timestamp = now - 3 * dayMs
            ))
            repository.insertWorkoutLog(WorkoutLog(
                workoutType = "Strength",
                exerciseName = "Dumbbell Chest Bench Press",
                sets = 3,
                reps = 12,
                weightKg = 24f,
                durationMinutes = 40,
                caloriesBurned = 290,
                notes = "Finished all sets at target weight",
                timestamp = now - 4 * dayMs
            ))
        }
    }

    private fun calculateStreak(workouts: List<WorkoutLog>): Int {
        if (workouts.isEmpty()) return 0
        
        val calendar = Calendar.getInstance()
        val workoutDays = workouts.map {
            calendar.timeInMillis = it.timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()

        var streak = 0
        val checkCalendar = Calendar.getInstance()
        
        var todayWorked = false
        val todayKey = "${checkCalendar.get(Calendar.YEAR)}-${checkCalendar.get(Calendar.DAY_OF_YEAR)}"
        if (workoutDays.contains(todayKey)) {
            todayWorked = true
        }

        // Check back from today or yesterday
        val baseCalendar = Calendar.getInstance()
        if (!todayWorked) {
            baseCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = "${baseCalendar.get(Calendar.YEAR)}-${baseCalendar.get(Calendar.DAY_OF_YEAR)}"
            if (workoutDays.contains(key)) {
                streak++
                baseCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        if (todayWorked) {
            streak++
        }
        return streak
    }
}

data class DashboardStats(
    val totalWorkouts: Int = 0,
    val totalActiveMinutes: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val currentWeightKg: Float = 75.0f,
    val weightChangeKg: Float = 0.0f,
    val streakDays: Int = 0
)
