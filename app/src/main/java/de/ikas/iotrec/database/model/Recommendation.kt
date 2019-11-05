package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.*
import com.squareup.moshi.Json
import de.ikas.iotrec.network.model.Context
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

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
    var invokeRec: Boolean,

    //var context: Context


    var context_temperature_raw: Int,
    var context_weather_raw: String,
    var context_length_of_trip_raw: Int
) : Parcelable