package de.ikas.iotrec.account.data.model

import com.squareup.moshi.Json

data class User(
    @field:Json(name = "username") val username: String
)