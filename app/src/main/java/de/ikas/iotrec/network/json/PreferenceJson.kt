package de.ikas.iotrec.network.json

data class PreferenceJson(
    var id: String,
    var category: String,
    var value: Int,
    var user: Int
)