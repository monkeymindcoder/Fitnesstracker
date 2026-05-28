package com.example.data

import kotlinx.coroutines.flow.Flow

class FitnessRepository(private val fitnessDao: FitnessDao) {
    val allWorkoutLogs: Flow<List<WorkoutLog>> = fitnessDao.getAllWorkoutLogs()
    val allWeightLogs: Flow<List<WeightLog>> = fitnessDao.getAllWeightLogs()

    suspend fun insertWorkoutLog(log: WorkoutLog) {
        fitnessDao.insertWorkoutLog(log)
    }

    suspend fun deleteWorkoutLogById(id: Int) {
        fitnessDao.deleteWorkoutLogById(id)
    }

    suspend fun insertWeightLog(log: WeightLog) {
        fitnessDao.insertWeightLog(log)
    }

    suspend fun deleteWeightLogById(id: Int) {
        fitnessDao.deleteWeightLogById(id)
    }
}
