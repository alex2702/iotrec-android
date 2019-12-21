package de.ikas.iotrec.network.adapter

import com.squareup.moshi.ToJson
import com.squareup.moshi.FromJson
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Experiment
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.network.json.CategoryJson
import de.ikas.iotrec.network.json.ExperimentJson
import de.ikas.iotrec.network.json.PreferenceJson
import de.ikas.iotrec.network.json.RecommendationJson
import de.ikas.iotrec.network.model.Context
import java.util.*
import kotlin.math.exp

/**
 * Used by Moshi to convert between JSON and Kotlin objects
 */
internal class ExperimentJsonAdapter {
    @FromJson
    fun experimentFromJson(experimentJson: ExperimentJson): Experiment {
        val experiment = Experiment(
            experimentJson.id,
            experimentJson.start,
            experimentJson.end,
            experimentJson.context_active,
            experimentJson.preferences_active,
            experimentJson.order,
            experimentJson.scenario,
            "CLOUDY",
            10,
            180,
            "NOON",
            null
        )
        return experiment
    }

    @ToJson
    fun experimentToJson(experiment: Experiment): ExperimentJson {
        val json = ExperimentJson(
            experiment.id,
            experiment.start,
            experiment.end,
            experiment.contextActive,
            experiment.preferencesActive,
            experiment.expOrder,
            experiment.scenario,
            Context(
                experiment.context_temperature,
                experiment.context_weather,
                experiment.context_length_of_trip,
                experiment.context_time_of_day,
                experiment.context_crowdedness
            )
        )
        return json
    }
}