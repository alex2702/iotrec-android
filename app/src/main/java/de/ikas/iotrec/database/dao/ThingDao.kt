package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Thing
import java.util.*

@Dao
interface ThingDao {

    @Query("SELECT * from thing_table WHERE id = :id")
    fun getThing(id: String): Thing

    @Query("SELECT * from thing_table WHERE id = :id")
    fun getThingLive(id: String): LiveData<Thing>

    @Query("SELECT * from thing_table ORDER BY id ASC")
    fun getAllThings(): LiveData<List<Thing>>

    @Query("SELECT * from thing_table WHERE inRange = 1 ORDER BY distance ASC, lastQueried DESC, lastSeen DESC")
    fun getThingsInRange(): LiveData<List<Thing>>

    @Query("SELECT * from thing_table WHERE inRange = 1 ORDER BY distance ASC, lastQueried DESC, lastSeen DESC")
    fun getThingsInRangeList(): List<Thing>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(thing: Thing)

    @Query("UPDATE thing_table SET inRange = :inRange WHERE id = :id")
    suspend fun setThingInRange(id: String, inRange: Boolean)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(thing: Thing)

    @Query("UPDATE thing_table SET inRange = :inRange, distance = :distance, rssi = :rssi, txPower = :txPower, lastSeen = :lastSeen WHERE id = :id")
    suspend fun updateBluetoothData(id: String, inRange: Boolean, distance: Double, rssi: Int, txPower: Int, lastSeen: Date)

    @Query("UPDATE thing_table SET " +
            "title = :title, " +
            "description = :description, " +
            "lastQueried = :lastQueried, " +
            "lastTriedToQuery = :lastTriedToQuery, " +
            "lastRecommended = :lastRecommended, " +
            "lastCheckedForRecommendation = :lastCheckedForRecommendation, " +
            "categories = :categories, " +
            "image = :image, " +
            "occupation = :occupation " +
            "WHERE id = :id")
    suspend fun updateBackendData(id: String, title: String, description: String, lastQueried: Date, lastTriedToQuery: Date, lastRecommended: Date, lastCheckedForRecommendation: Date, image: String, categories: String, occupation: Int)

    @Query("DELETE FROM thing_table")
    fun deleteAll()

    @Query("UPDATE thing_table SET lastRecommended = :lastRecommended WHERE id = :id")
    suspend fun updateLastRecommended(id: String, lastRecommended: Date)

    @Query("UPDATE thing_table SET lastCheckedForRecommendation = :lastCheckedForRecommendation WHERE id = :id")
    suspend fun updateLastCheckedForRecommendation(id: String, lastCheckedForRecommendation: Date)

    @Query("UPDATE thing_table SET recommendationQueryRunning = :queryRunning WHERE id = :id")
    suspend fun setRecommendationQueryRunnng(id: String, queryRunning: Boolean)
}