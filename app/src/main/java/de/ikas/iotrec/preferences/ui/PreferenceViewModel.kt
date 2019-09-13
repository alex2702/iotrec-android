package de.ikas.iotrec.preferences.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferenceViewModel public constructor(application: Application) : AndroidViewModel(application) {

    private val TAG = "PreferenceViewModel"

    private val repository: CategoryRepository
    val topLevelCategories: LiveData<List<Category>>
    var subCategories: LiveData<List<Category>>
    var selectedSubCategories: LiveData<List<Category>>

    init {
        //val categoriesDao = IotRecDatabase.getDatabase(application, viewModelScope).categoryDao()
        //repository = CategoryRepository(categoriesDao)
        val app = application as IotRecApplication
        repository = app.categoryRepository

        topLevelCategories = repository.topLevelCategories
        subCategories = repository.subCategories
        selectedSubCategories = repository.selectedSubCategories
    }

    fun updateSubCategories(categoryId: String) = viewModelScope.launch(Dispatchers.IO) {
        subCategories = repository.getSubCategories(categoryId)
        //repository.updateSubCategories(categoryId)
    }

    fun updateSelectedSubCategories(categoryId: String) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "updateSelectedSubCategories with categoryId $categoryId")
        selectedSubCategories = repository.getSelectedSubCategories(categoryId)
        //repository.updateSelectedSubCategories(categoryId)
    }
}