package de.ikas.iotrec.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Reply

@Dao
interface ReplyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reply: Reply)

    @Query("DELETE FROM reply_table")
    fun deleteAll()
}