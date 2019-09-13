package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.model.Thing
import java.util.*

class ThingRepository(private val thingDao: ThingDao) {
    val allThings: LiveData<List<Thing>> = thingDao.getAllThings()
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
        thingDao.updateBluetoothData(thing.id, thing.inRange, thing.distance, thing.rssi, thing.txPower, thing.lastSeen)
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
    suspend fun setThingInRange(id: String, inRange: Boolean) {
        return thingDao.setThingInRange(id, inRange)
    }

    @WorkerThread
    suspend fun updateBackendData(id: String, title: String, description: String, lastQueried: Date, lastTriedToQuery: Date) {
        return thingDao.updateBackendData(id, title, description, lastQueried, lastTriedToQuery)
    }
}