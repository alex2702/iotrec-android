package de.ikas.iotrec.network.json

data class RatingJson(
    var id: String,
    var recommendation: String,
    var value: Float,
    var improvements: String
)