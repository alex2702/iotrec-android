package de.ikas.iotrec.network.adapter

import com.squareup.moshi.ToJson
import com.squareup.moshi.FromJson
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.network.json.CategoryJson

/**
 * Used by Moshi to convert between JSON and Kotlin objects
 */
internal class CategoryJsonAdapter {
    @FromJson
    fun categoryFromJson(categoryJson: CategoryJson): Category {
        val category = Category(
            categoryJson.text_id,
            categoryJson.name,
            categoryJson.parent,
            categoryJson.level,
            categoryJson.selected
        )
        return category
    }

    @ToJson
    fun categoryToJson(category: Category): CategoryJson {
        val json = CategoryJson(
            category.textId,
            category.name,
            category.parentTextId,
            category.level,
            category.selected
        )
        return json
    }
}