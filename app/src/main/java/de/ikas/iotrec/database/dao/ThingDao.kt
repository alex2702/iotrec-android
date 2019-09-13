package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Thing
import java.util.*

@Dao
interface ThingDao {

    @Query("SELECT * from thing_table WHERE id = :id")
    fun getThing(id: String): Thing

    @Query("SELECT * from thing_table ORDER BY id ASC")
    fun getAllThings(): LiveData<List<Thing>>

    @Query("SELECT * from thing_table WHERE inRange = 1")
    fun getThingsInRange(): LiveData<List<Thing>>

    @Query("SELECT * from thing_table WHERE inRange = 1")
    fun getThingsInRangeList(): List<Thing>

    // TODO
    // is IGNORE the best strategy here?
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(thing: Thing)

    @Query("UPDATE thing_table SET inRange = :inRange WHERE id = :id")
    suspend fun setThingInRange(id: String, inRange: Boolean)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(thing: Thing)

    @Query("UPDATE thing_table SET inRange = :inRange, distance = :distance, rssi = :rssi, txPower = :txPower, lastSeen = :lastSeen WHERE id = :id")
    suspend fun updateBluetoothData(id: String, inRange: Boolean, distance: Double, rssi: Int, txPower: Int, lastSeen: Date)

    @Query("UPDATE thing_table SET title = :title, description = :description, lastQueried = :lastQueried, lastTriedToQuery = :lastTriedToQuery WHERE id = :id")
    suspend fun updateBackendData(id: String, title: String, description: String, lastQueried: Date, lastTriedToQuery: Date)
    // TODO extend by more fields when available in backend

    @Query("DELETE FROM thing_table")
    fun deleteAll()
}