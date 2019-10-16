package de.ikas.iotrec.database.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.dao.PreferenceDao
import de.ikas.iotrec.database.dao.RecommendationDao
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.database.model.Thing
import java.util.*

class RecommendationRepository(private val recommendationDao: RecommendationDao) {

    private val TAG = "RecommendationRepository"

    @WorkerThread
    suspend fun insert(recommendation: Recommendation) {
        recommendationDao.insert(recommendation)
    }

    @WorkerThread
    fun deleteAll() {
        recommendationDao.deleteAll()
    }
}