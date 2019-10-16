package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.*
import com.squareup.moshi.Json
import de.ikas.iotrec.account.data.model.User
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "recommendation_table"
)
data class Recommendation(
    @PrimaryKey
    var id: String,

    var thing: String,

    var score: Float,

    @Json(name = "invoke_rec")
    var invokeRec: Boolean
) : Parcelable