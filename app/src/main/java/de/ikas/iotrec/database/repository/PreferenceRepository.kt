package de.ikas.iotrec.database.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.dao.PreferenceDao
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Thing
import java.util.*

class PreferenceRepository(private val preferenceDao: PreferenceDao, private val categoryDao: CategoryDao) {

    private val TAG = "PreferenceRepository"
    var preferences: LiveData<List<Preference>> = preferenceDao.getPreferencesInCategory("Root")

    @WorkerThread
    suspend fun insert(preference: Preference) {
        preferenceDao.insert(preference)
    }

    @WorkerThread
    suspend fun delete(preferenceId: String) {
        preferenceDao.delete(preferenceId)
    }

    @WorkerThread
    fun deleteAll() {
        preferenceDao.deleteAll()
    }

    @WorkerThread
    suspend fun getPreferencesInCategory(categoryId: String): LiveData<List<Preference>> {
        return preferenceDao.getPreferencesInCategory(categoryId)
    }

    @WorkerThread
    fun getNumberOfPreferences(categoryId: String): Int {
        return preferenceDao.getNumberOfPreferences(categoryId)
    }

    @WorkerThread
    fun getCurrentPreferences(): LiveData<List<Preference>> {
        return preferenceDao.getCurrentPreferences()
    }
}