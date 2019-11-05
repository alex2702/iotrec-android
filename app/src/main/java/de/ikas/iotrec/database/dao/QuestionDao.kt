package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Category
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Question

@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(vararg questions: Question)

    @Query("SELECT * from question_table")
    fun getQuestions(): LiveData<List<Question>>
}