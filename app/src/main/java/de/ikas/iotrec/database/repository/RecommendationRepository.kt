package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import de.ikas.iotrec.database.dao.RecommendationDao
import de.ikas.iotrec.database.model.Recommendation

class RecommendationRepository(private val recommendationDao: RecommendationDao) {

    @WorkerThread
    suspend fun insert(recommendation: Recommendation) {
        recommendationDao.insert(recommendation)
    }

    @WorkerThread
    fun deleteAll() {
        recommendationDao.deleteAll()
    }
}