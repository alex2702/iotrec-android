package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Category
import androidx.room.OnConflictStrategy
import de.ikas.iotrec.database.model.Preference

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: Preference)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(vararg preferences: Preference)

    @Query("DELETE FROM preference_table WHERE id = :preferenceId")
    suspend fun delete(preferenceId: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(preference: Preference)

    @Query("DELETE FROM preference_table")
    fun deleteAll()

    @Query("SELECT * FROM preference_table WHERE category IN (SELECT textId FROM category_table WHERE parentTextId = :categoryId)")
    fun getPreferencesInCategory(categoryId: String): LiveData<List<Preference>>

    @Query("SELECT COUNT(*) FROM preference_table WHERE category IN (SELECT textId FROM category_table WHERE parentTextId = :categoryId)")
    fun getNumberOfPreferences(categoryId: String): Int

    @Query("SELECT * FROM preference_table")
    fun getCurrentPreferences(): LiveData<List<Preference>>

    @Query("SELECT * FROM preference_table")
    suspend fun getCurrentPreferencesList(): List<Preference>
}