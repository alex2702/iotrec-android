package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.*
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "preference_table"
)
data class Preference(
    @PrimaryKey var category: String,
    var value: Int,
    var user: Int
) : Parcelable