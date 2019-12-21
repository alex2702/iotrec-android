package de.ikas.iotrec.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Feedback

@Dao
interface FeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feedback: Feedback)

    @Query("DELETE FROM feedback_table")
    fun deleteAll()
}