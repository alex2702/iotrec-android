package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "reply_table"
)
data class Reply(
    @PrimaryKey var id: Int,
    var question: Int,
    var value: Int
) : Parcelable