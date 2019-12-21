package de.ikas.iotrec.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Recommendation

@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendation: Recommendation)

    @Query("DELETE FROM recommendation_table")
    fun deleteAll()
}