package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.dao.PreferenceDao
import de.ikas.iotrec.database.model.Preference

class PreferenceRepository(private val preferenceDao: PreferenceDao, private val categoryDao: CategoryDao) {

    var preferences: LiveData<List<Preference>> = preferenceDao.getPreferencesInCategory("Root")

    @WorkerThread
    suspend fun insert(preference: Preference) {
        preferenceDao.insert(preference)
    }

    @WorkerThread
    suspend fun insertMultiple(vararg preferences: Preference) {
        preferenceDao.insertMultiple(*preferences)
    }

    @WorkerThread
    suspend fun update(preference: Preference) {
        preferenceDao.update(preference)
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
    suspend fun getCurrentPreferencesList(): List<Preference> {
        return preferenceDao.getCurrentPreferencesList()
    }
}