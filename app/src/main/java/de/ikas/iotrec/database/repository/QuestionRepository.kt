package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.*
import de.ikas.iotrec.database.model.*
import de.ikas.iotrec.network.IotRecApiInit
import retrofit2.Response

class QuestionRepository(
    private val iotRecApi: IotRecApiInit,
    private val questionDao: QuestionDao
) {

    private val TAG = "QuestionRepository"

    val questions: LiveData<List<Question>> = questionDao.getQuestions()

    //@WorkerThread
    //suspend fun insert(rating: Rating) {
    //    experimentDao.insert(rating)
    //}

    @WorkerThread
    suspend fun insertMultiple(vararg questions: Question) {
        questionDao.insertMultiple(*questions)
    }

    /*
    @WorkerThread
    fun deleteAll() {
        experimentDao.deleteAll()
    }

    @WorkerThread
    suspend fun getExperimentByOrder(order: Int): Experiment {
        return experimentDao.getExperimentByOrder(order)
    }

    @WorkerThread
    suspend fun getAllExperiments(): List<Experiment> {
        return experimentDao.getAllExperiments()
    }

    @WorkerThread
    suspend fun setExperimentStartDate(experiment: Experiment): Response<Experiment> {
        experimentDao.update(experiment)
        return iotRecApi.updateExperiment(experiment)
    }
    */
}