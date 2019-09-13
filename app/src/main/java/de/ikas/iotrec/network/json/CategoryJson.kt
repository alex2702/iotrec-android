package de.ikas.iotrec.network.json

data class CategoryJson(
    var text_id: String,
    var name: String,
    var parent: String,
    var level: Int,
    var selected: Boolean
)