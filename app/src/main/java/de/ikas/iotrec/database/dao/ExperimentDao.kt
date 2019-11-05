package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Category
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Experiment
import de.ikas.iotrec.database.model.Preference

@Dao
interface ExperimentDao {
    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //suspend fun insert(experiment: Experiment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(vararg experiments: Experiment)

    //@Update(onConflict = OnConflictStrategy.REPLACE)
    //suspend fun update(experiment: Experiment)

    @Query("DELETE FROM experiment_table")
    fun deleteAll()

    @Query("SELECT * from experiment_table WHERE expOrder = :order")
    suspend fun getExperimentByOrder(order: Int): Experiment

    @Query("SELECT * from experiment_table")
    suspend fun getAllExperiments(): List<Experiment>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(experiment: Experiment)
}