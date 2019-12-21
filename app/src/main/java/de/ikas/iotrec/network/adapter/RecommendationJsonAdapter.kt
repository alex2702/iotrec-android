package de.ikas.iotrec.network.adapter

import com.squareup.moshi.ToJson
import com.squareup.moshi.FromJson
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.network.json.CategoryJson
import de.ikas.iotrec.network.json.PreferenceJson
import de.ikas.iotrec.network.json.RecommendationJson
import de.ikas.iotrec.network.model.Context

/**
 * Used by Moshi to convert between JSON and Kotlin objects
 */
internal class RecommendationJsonAdapter {
    @FromJson
    fun recommendationFromJson(recommendationJson: RecommendationJson): Recommendation {
        val recommendation = Recommendation(
            recommendationJson.id,
            recommendationJson.thing,
            recommendationJson.score,
            recommendationJson.invoke_rec,
            recommendationJson.context.temperature_raw,
            recommendationJson.context.weather_raw,
            recommendationJson.context.length_of_trip_raw,
            recommendationJson.context.time_of_day_raw,
            recommendationJson.context.crowdedness_raw,
            recommendationJson.experiment
        )
        return recommendation
    }

    @ToJson
    fun recommendationToJson(recommendation: Recommendation): RecommendationJson {
        val json = RecommendationJson(
            recommendation.id,
            recommendation.thing,
            recommendation.score,
            recommendation.invokeRec,
            Context(
                recommendation.context_temperature_raw,
                recommendation.context_weather_raw,
                recommendation.context_length_of_trip_raw,
                recommendation.context_time_of_day_raw,
                recommendation.context_crowdedness_raw
            ),
            recommendation.experiment
        )
        return json
    }
}