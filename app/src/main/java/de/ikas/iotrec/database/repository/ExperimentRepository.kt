package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import de.ikas.iotrec.database.dao.*
import de.ikas.iotrec.database.model.*
import de.ikas.iotrec.network.IotRecApiInit
import retrofit2.Response

class ExperimentRepository(
    private val iotRecApi: IotRecApiInit,
    private val experimentDao: ExperimentDao
) {

    private val TAG = "ExperimentRepository"

    //@WorkerThread
    //suspend fun insert(rating: Rating) {
    //    experimentDao.insert(rating)
    //}

    @WorkerThread
    suspend fun insertMultiple(vararg experiments: Experiment) {
        experimentDao.insertMultiple(*experiments)
    }

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
    suspend fun updateExperiment(experiment: Experiment): Response<Experiment> {
        experimentDao.update(experiment)
        return iotRecApi.updateExperiment(experiment)
    }
}