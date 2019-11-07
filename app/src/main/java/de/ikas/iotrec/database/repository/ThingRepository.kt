package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.model.Thing
import java.util.*

class ThingRepository(private val thingDao: ThingDao) {
    val allThings: LiveData<List<Thing>> = thingDao.getAllThings()
    //val allThingsInRange: LiveData<List<Thing>> = thingDao.getThingsInRangeRecently(Date(Date().time - 60 * 1000))
    val allThingsInRange: LiveData<List<Thing>> = thingDao.getThingsInRange()

    @WorkerThread
    suspend fun insert(thing: Thing) {
        thingDao.insert(thing)
    }

    @WorkerThread
    suspend fun update(thing: Thing) {
        thingDao.update(thing)
    }

    @WorkerThread
    suspend fun updateBluetoothData(thing: Thing) {
        thingDao.updateBluetoothData(thing.id, thing.inRange!!, thing.distance!!, thing.rssi!!, thing.txPower!!, thing.lastSeen!!)
    }

    @WorkerThread
    suspend fun getThingsInRangeList(): Collection<Thing> {
        return thingDao.getThingsInRangeList()
    }

    @WorkerThread
    suspend fun getThing(id: String): Thing {
        return thingDao.getThing(id)
    }

    @WorkerThread
    suspend fun getThingLive(id: String): LiveData<Thing> {
        return thingDao.getThingLive(id)
    }

    @WorkerThread
    suspend fun setThingInRange(id: String, inRange: Boolean) {
        return thingDao.setThingInRange(id, inRange)
    }

    @WorkerThread
    suspend fun updateBackendData(id: String, title: String, description: String, lastQueried: Date, lastTriedToQuery: Date, lastRecommended: Date, lastCheckedForRecommendation: Date, image: String, categories: String, occupation: Int) {
        return thingDao.updateBackendData(id, title, description, lastQueried, lastTriedToQuery, lastRecommended, lastCheckedForRecommendation, image, categories, occupation)
    }

    @WorkerThread
    suspend fun updateLastRecommended(id: String, lastRecommended: Date) {
        return thingDao.updateLastRecommended(id, lastRecommended)
    }

    @WorkerThread
    suspend fun updateLastCheckedForRecommendation(id: String, lastCheckedForRecommendation: Date) {
        return thingDao.updateLastCheckedForRecommendation(id, lastCheckedForRecommendation)
    }

    @WorkerThread
    fun deleteAll() {
        thingDao.deleteAll()
    }

    @WorkerThread
    suspend fun setRecommendationQueryRunning(id: String, queryRunning: Boolean) {
        return thingDao.setRecommendationQueryRunnng(id, queryRunning)
    }
}