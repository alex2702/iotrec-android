package de.ikas.iotrec.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Experiment

@Dao
interface ExperimentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(vararg experiments: Experiment)

    @Query("DELETE FROM experiment_table")
    fun deleteAll()

    @Query("SELECT * from experiment_table WHERE expOrder = :order")
    suspend fun getExperimentByOrder(order: Int): Experiment

    @Query("SELECT * from experiment_table")
    suspend fun getAllExperiments(): List<Experiment>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(experiment: Experiment)
}