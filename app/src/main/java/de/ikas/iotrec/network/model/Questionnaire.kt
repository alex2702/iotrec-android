package de.ikas.iotrec.network.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Questionnaire(
    var age: String?,
    var gender: String?,
    var qualification: String?,
    var smartphone_usage: String?
) : Parcelable