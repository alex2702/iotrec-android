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
    tableName = "question_table"
)
data class Question(
    @PrimaryKey
    var id: Int,
    @Json(name = "short_name")
    var shortName: String,
    var text: String
) : Parcelable