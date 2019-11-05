package de.ikas.iotrec.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(
    tableName = "experiment_table"
)
data class Experiment(
    @PrimaryKey var id: Int,
    var start: Date?,
    var end: Date?,
    @Json(name = "context_active")
    var contextActive: Boolean,
    @Json(name = "preferences_active")
    var preferencesActive: Boolean,
    @Json(name = "order")
    var expOrder: Int,
    var scenario: String,
    var context_weather: String,
    var context_temperature: Int,
    var context_length_of_trip: Int,
    var context_time_of_day: String?,
    var context_crowdedness: String?
    ) : Parcelable