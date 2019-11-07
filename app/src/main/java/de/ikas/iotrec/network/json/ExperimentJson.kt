package de.ikas.iotrec.network.json

import de.ikas.iotrec.network.model.Context
import java.util.*

data class ExperimentJson(
    var id: Int,
    var start: Date?,
    var end: Date?,
    var context_active: Boolean,
    var preferences_active: Boolean,
    var order: Int,
    var scenario: String,
    var context: Context?
)