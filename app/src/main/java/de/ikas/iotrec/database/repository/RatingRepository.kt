package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import de.ikas.iotrec.database.dao.*
import de.ikas.iotrec.database.model.*

class RatingRepository(private val ratingDao: RatingDao) {

    @WorkerThread
    suspend fun insert(rating: Rating) {
        ratingDao.insert(rating)
    }

    @WorkerThread
    fun deleteAll() {
        ratingDao.deleteAll()
    }
}