package de.ikas.iotrec.database.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.model.Category

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()
    val topLevelCategories: LiveData<List<Category>> = categoryDao.getTopLevelCategories()
    var subCategories: LiveData<List<Category>> = categoryDao.getSubCategories("Root")

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
    suspend fun getSubCategories(categoryId: String): LiveData<List<Category>> {
        subCategories = categoryDao.getSubCategories(categoryId)
        return subCategories
    }

    @WorkerThread
    fun deleteAll() {
        categoryDao.deleteAll()
    }
}