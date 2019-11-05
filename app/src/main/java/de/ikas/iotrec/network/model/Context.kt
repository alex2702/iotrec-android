package de.ikas.iotrec.network.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Context(
    var temperature_raw: Int,
    var weather_raw: String,
    var length_of_trip_raw: Int,
    var time_of_day_raw: String?,
    var crowdedness_raw: String?
) : Parcelable