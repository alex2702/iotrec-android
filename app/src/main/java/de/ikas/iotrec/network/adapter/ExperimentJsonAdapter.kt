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


internal class ExperimentJsonAdapter {
    @FromJson
    fun experimentFromJson(experimentJson: ExperimentJson): Experiment {
        //val startDate = if(experimentJson.start != null) experimentJson.start else Date()
        //val endDate = if(experimentJson.end != null) experimentJson.end else Date()

        val experiment = Experiment(
            experimentJson.id,
            experimentJson.start,
            experimentJson.end,
            experimentJson.context_active,
            experimentJson.preferences_active,
            experimentJson.order,
            experimentJson.scenario,
            experimentJson.context.weather_raw,
            experimentJson.context.temperature_raw,
            experimentJson.context.length_of_trip_raw,
            experimentJson.context.time_of_day_raw,
            experimentJson.context.crowdedness_raw
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