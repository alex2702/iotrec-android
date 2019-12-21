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
    tableName = "category_table"
)
data class Category(

    @PrimaryKey
    @Json(name = "text_id")
    var textId: String,

    var name: String,

    @Json(name = "parent")
    var parentTextId: String = "",

    var level: Int,

    var selected: Boolean = false
) : Parcelable