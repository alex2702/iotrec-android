package de.ikas.iotrec.account.data.model

import com.squareup.moshi.Json
import de.ikas.iotrec.database.model.Preference

data class User(
    @field:Json(name = "id")
    var id: Int,

    @field:Json(name = "username")
    var username: String,

    @field:Json(name = "email")
    var email: String,

    @field:Json(name = "preferences")
    var preferences: MutableList<Preference>
)