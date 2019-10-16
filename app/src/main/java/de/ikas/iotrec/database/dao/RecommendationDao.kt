package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Category
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Recommendation

@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendation: Recommendation)

    @Query("DELETE FROM recommendation_table")
    fun deleteAll()
}