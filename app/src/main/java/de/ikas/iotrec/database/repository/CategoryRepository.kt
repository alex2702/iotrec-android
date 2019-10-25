package de.ikas.iotrec.database.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.dao.ThingDao
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Thing
import java.util.*

class CategoryRepository(private val categoryDao: CategoryDao) {

    private val TAG = "CategoryRepository"

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()
    val topLevelCategories: LiveData<List<Category>> = categoryDao.getTopLevelCategories()
    var subCategories: LiveData<List<Category>> = categoryDao.getSubCategories("Root")
    //var selectedSubCategories: LiveData<List<Category>> = categoryDao.getSelectedSubCategories("")

    @WorkerThread
    suspend fun insert(category: Category) {
        categoryDao.insert(category)
    }

    @WorkerThread
    suspend fun insertMultiple(vararg categories: Category) {
        categoryDao.insertMultiple(*categories)
    }

    @WorkerThread
    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    @WorkerThread
    suspend fun getCategory(id: String): Category {
        return categoryDao.getCategory(id)
    }

    @WorkerThread
    suspend fun getCategories(ids: List<String>): List<Category> {
        return categoryDao.getCategories(ids)
    }

    @WorkerThread
    suspend fun updateSubCategories(categoryId: String) {
        subCategories = categoryDao.getSubCategories(categoryId)
    }

    @WorkerThread
    suspend fun getSubCategories(categoryId: String): LiveData<List<Category>> {
        subCategories = categoryDao.getSubCategories(categoryId)
        return subCategories
    }

    @WorkerThread
    fun getNumberOfSubCategories(categoryId: String): Int {
        return categoryDao.getNumberOfSubCategories(categoryId)
    }

    /*
    @WorkerThread
    suspend fun getSelectedSubCategories(categoryId: String): LiveData<List<Category>> {
        selectedSubCategories = categoryDao.getSelectedSubCategories(categoryId)
        return selectedSubCategories
    }

    @WorkerThread
    suspend fun updateSelectedSubCategories(categoryId: String) {
        selectedSubCategories = categoryDao.getSelectedSubCategories(categoryId)
    }

    @WorkerThread
    suspend fun setCategorySelected(categoryId: String, selected: Boolean): Int {
        return categoryDao.setCategorySelected(categoryId, selected)
    }

    @WorkerThread
    suspend fun setCategoriesSelectedTrue(categories: List<String>): Int {
        Log.d(TAG, "setCategoriesSelectedTrue: " + categories.size.toString() + " - " + categories.toString())
        return categoryDao.setCategoriesSelectedTrue(categories)
    }

    @WorkerThread
    suspend fun setAllCategoriesSelectedFalse(): Int {
        return categoryDao.setAllCategoriesSelectedFalse()
    }
    */




    /*
    fun setSelectedSubCategories(selectedCategories: List<String>) {
        selectedSubCategories = selectedCategories
    }

    fun getSelectedSubCategories(): LiveData<List<String>> {
        return selectedSubCategories
    }
    */

    @WorkerThread
    fun deleteAll() {
        categoryDao.deleteAll()
    }
}