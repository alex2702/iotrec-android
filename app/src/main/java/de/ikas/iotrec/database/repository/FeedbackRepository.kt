package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import de.ikas.iotrec.database.dao.FeedbackDao
import de.ikas.iotrec.database.model.Feedback

class FeedbackRepository(private val feedbackDao: FeedbackDao) {

    @WorkerThread
    suspend fun insert(feedback: Feedback) {
        feedbackDao.insert(feedback)
    }

    @WorkerThread
    fun deleteAll() {
        feedbackDao.deleteAll()
    }
}