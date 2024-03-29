package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Category
import androidx.room.OnConflictStrategy

@Dao
interface CategoryDao {

    @Query("SELECT * from category_table WHERE textId = :id")
    fun getCategory(id: String): Category

    @Query("SELECT * from category_table ORDER BY textId ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * from category_table WHERE textId IN (:ids) ORDER BY textId ASC")
    suspend fun getCategories(ids: List<String>): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(vararg categories: Category)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(category: Category)

    @Query("DELETE FROM category_table")
    fun deleteAll()

    @Query("SELECT * from category_table WHERE level = 1")
    fun getTopLevelCategories(): LiveData<List<Category>>

    @Query("SELECT * from category_table WHERE parentTextId = :categoryId")
    fun getSubCategories(categoryId: String): LiveData<List<Category>>

    @Query("SELECT COUNT(*) FROM category_table WHERE parentTextId = :categoryId")
    fun getNumberOfSubCategories(categoryId: String): Int
}