package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "rating_table"
)
data class Rating(
    @PrimaryKey var id: String,
    var value: Float,
    var improvements: ArrayList<String>,
    var recommendation: String
) : Parcelable