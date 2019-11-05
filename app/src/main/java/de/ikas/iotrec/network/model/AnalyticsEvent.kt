package de.ikas.iotrec.network.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AnalyticsEvent(
    var type: String,
    var recommendation: String?,
    var thing: String?,
    var value: Float?
) : Parcelable