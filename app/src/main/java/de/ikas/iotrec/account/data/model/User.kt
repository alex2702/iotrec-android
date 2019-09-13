package de.ikas.iotrec.account.data.model

import com.squareup.moshi.Json

data class User(
    @field:Json(name = "username")
    var username: String,

    @field:Json(name = "email")
    var email: String,

    @field:Json(name = "preferences")
    var preferences: MutableList<String>
)