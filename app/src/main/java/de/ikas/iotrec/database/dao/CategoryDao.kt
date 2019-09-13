package de.ikas.iotrec.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Thing
import java.util.*
import de.ikas.iotrec.account.data.model.User
import androidx.room.OnConflictStrategy



@Dao
interface CategoryDao {

    @Query("SELECT * from category_table WHERE textId = :id")
    fun getCategory(id: String): Category

    @Query("SELECT * from category_table ORDER BY textId ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiple(vararg categories: Category)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(category: Category)

    @Query("DELETE FROM category_table")
    fun deleteAll()

    @Query("SELECT * from category_table WHERE level = 0")
    fun getTopLevelCategories(): LiveData<List<Category>>

    @Query("SELECT * from category_table WHERE parentTextId = :categoryId")
    fun getSubCategories(categoryId: String): LiveData<List<Category>>

    @Query("SELECT * from category_table WHERE parentTextId = :categoryId AND selected = 1")
    fun getSelectedSubCategories(categoryId: String): LiveData<List<Category>>

    @Query("UPDATE category_table SET selected = :selected WHERE textId = :categoryId")
    suspend fun setCategorySelected(categoryId: String, selected: Boolean): Int

    @Query("UPDATE category_table SET selected = 1 WHERE textId IN (:categories)")
    suspend fun setCategoriesSelectedTrue(categories: List<String>): Int

    @Query("UPDATE category_table SET selected = 0")
    suspend fun setAllCategoriesSelectedFalse(): Int
}