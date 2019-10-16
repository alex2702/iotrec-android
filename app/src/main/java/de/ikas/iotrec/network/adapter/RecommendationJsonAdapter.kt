package de.ikas.iotrec.network.adapter

import com.squareup.moshi.ToJson
import com.squareup.moshi.FromJson
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.network.json.CategoryJson
import de.ikas.iotrec.network.json.PreferenceJson
import de.ikas.iotrec.network.json.RecommendationJson


internal class RecommendationJsonAdapter {
    @FromJson
    fun recommendationFromJson(recommendationJson: RecommendationJson): Recommendation {
        val recommendation = Recommendation(
            recommendationJson.id,
            recommendationJson.thing,
            recommendationJson.score,
            recommendationJson.invokeRec
        )
        return recommendation
    }

    @ToJson
    fun recommendationToJson(recommendation: Recommendation): RecommendationJson {
        val json = RecommendationJson(
            recommendation.id,
            recommendation.thing,
            recommendation.score,
            recommendation.invokeRec
        )
        return json
    }
}