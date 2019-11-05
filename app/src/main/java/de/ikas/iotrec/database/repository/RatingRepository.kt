package de.ikas.iotrec.database.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.*
import de.ikas.iotrec.database.model.*
import java.util.*

class RatingRepository(private val ratingDao: RatingDao) {

    private val TAG = "RatingRepository"

    @WorkerThread
    suspend fun insert(rating: Rating) {
        ratingDao.insert(rating)
    }

    @WorkerThread
    fun deleteAll() {
        ratingDao.deleteAll()
    }
}