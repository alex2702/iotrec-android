package de.ikas.iotrec.account.ui

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val username: String,
    val email: String,
    val token: String,
    val preferences: List<String>
)
