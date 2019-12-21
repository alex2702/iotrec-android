package de.ikas.iotrec.preferences.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.repository.CategoryRepository
import de.ikas.iotrec.database.repository.PreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferenceViewModel public constructor(application: Application) : AndroidViewModel(application) {

    private val TAG = "PreferenceViewModel"

    private val categoryRepository: CategoryRepository
    private val preferenceRepository: PreferenceRepository
    val topLevelCategories: LiveData<List<Category>>
    var subCategories: LiveData<List<Category>>
    var preferences: LiveData<List<Preference>>

    init {
        val app = application as IotRecApplication
        categoryRepository = app.categoryRepository
        preferenceRepository = app.preferenceRepository

        topLevelCategories = categoryRepository.topLevelCategories
        subCategories = categoryRepository.subCategories
        preferences = preferenceRepository.preferences
    }

    // gets all sub categories of a parent category
    fun updateSubCategoriesInCategory(categoryId: String) = viewModelScope.launch(Dispatchers.IO) {
        subCategories = categoryRepository.getSubCategories(categoryId)
    }

    // gets all preferences within a category
    fun updatePreferencesInCategory(categoryId: String) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "updatePreferences with categoryId $categoryId")
        preferences = preferenceRepository.getPreferencesInCategory(categoryId)
    }
}