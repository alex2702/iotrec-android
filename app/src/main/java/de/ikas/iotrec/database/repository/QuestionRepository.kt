package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.*
import de.ikas.iotrec.database.model.*
import de.ikas.iotrec.network.IotRecApiInit
import retrofit2.Response

class QuestionRepository(
    private val questionDao: QuestionDao
) {

    private val TAG = "QuestionRepository"

    val questions: LiveData<List<Question>> = questionDao.getQuestions()

    @WorkerThread
    suspend fun insertMultiple(vararg questions: Question) {
        questionDao.insertMultiple(*questions)
    }


    @WorkerThread
    fun deleteAll() {
        questionDao.deleteAll()
    }
}