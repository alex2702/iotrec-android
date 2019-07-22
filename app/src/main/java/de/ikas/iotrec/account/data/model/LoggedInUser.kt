package de.ikas.iotrec.account.data.model

import com.squareup.moshi.Json

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    @field:Json(name = "token") val token: String,
    @field:Json(name = "user") val user: User
)
