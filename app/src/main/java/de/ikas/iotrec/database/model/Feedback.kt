package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "feedback_table"
)
data class Feedback(
    @PrimaryKey var id: String,
    var value: Int,
    var recommendation: String
) : Parcelable