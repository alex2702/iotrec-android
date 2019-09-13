package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.*
import java.util.*

@Parcelize
@Entity(
    tableName = "category_table"/*,
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["textId"],
            childColumns = ["parentTextId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(
            value = ["parentTextId"]
        )
    ]*/
)
data class Category(

    @PrimaryKey
    //@ColumnInfo(name = "text_id")
    @Json(name = "text_id")
    var textId: String,

    var name: String,

    //@ColumnInfo(name = "parent")
    @Json(name = "parent")
    var parentTextId: String = "",

    var level: Int,

    var selected: Boolean = false
) : Parcelable