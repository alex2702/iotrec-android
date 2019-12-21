package de.ikas.iotrec.bluetooth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.ThingRepository

class ThingViewModel public constructor(application: Application) : AndroidViewModel(application) {

    private val repository: ThingRepository
    // live data that holds all things currently in range
    val allThingsInRange: LiveData<List<Thing>>

    init {
        val thingsDao = IotRecDatabase.getDatabase(application, viewModelScope).thingDao()
        repository = ThingRepository(thingsDao)
        allThingsInRange = repository.allThingsInRange
    }
}