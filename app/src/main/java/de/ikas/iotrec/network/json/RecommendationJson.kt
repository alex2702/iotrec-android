package de.ikas.iotrec.network.json

import de.ikas.iotrec.network.model.Context

data class RecommendationJson(
    var id: String,
    var thing: String,
    var score: Float,
    var invoke_rec: Boolean,
    var context: Context
)