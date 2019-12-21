package de.ikas.iotrec.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Rating

@Dao
interface RatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rating: Rating)

    @Query("DELETE FROM rating_table")
    fun deleteAll()
}