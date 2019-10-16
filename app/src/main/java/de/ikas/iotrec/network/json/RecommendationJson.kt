package de.ikas.iotrec.network.json

import de.ikas.iotrec.database.model.Thing

data class RecommendationJson(
    var id: String,
    var thing: String,
    var score: Float,
    var invokeRec: Boolean
)