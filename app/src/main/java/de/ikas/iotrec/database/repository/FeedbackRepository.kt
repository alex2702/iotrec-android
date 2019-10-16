package de.ikas.iotrec.database.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.dao.FeedbackDao
import de.ikas.iotrec.database.dao.PreferenceDao
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Feedback
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Thing
import java.util.*

class FeedbackRepository(private val feedbackDao: FeedbackDao) {

    private val TAG = "FeedbackRepository"

    @WorkerThread
    suspend fun insert(feedback: Feedback) {
        feedbackDao.insert(feedback)
    }

    @WorkerThread
    fun deleteAll() {
        feedbackDao.deleteAll()
    }
}