package de.ikas.iotrec.network.adapter

import com.squareup.moshi.ToJson
import com.squareup.moshi.FromJson
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Rating
import de.ikas.iotrec.network.json.CategoryJson
import de.ikas.iotrec.network.json.PreferenceJson
import de.ikas.iotrec.network.json.RatingJson


internal class RatingJsonAdapter {
    @FromJson
    fun ratingFromJson(ratingJson: RatingJson): Rating {
        val improvementsList = arrayListOf<String>()
        improvementsList.addAll(ratingJson.improvements.let { it.split(";").map { x -> x.trim() } })
        val rating = Rating(
            ratingJson.id,
            ratingJson.value,
            improvementsList,
            ratingJson.recommendation
        )
        return rating
    }

    @ToJson
    fun ratingToJson(rating: Rating): RatingJson {
        val json = RatingJson(
            rating.id,
            rating.recommendation,
            rating.value,
            rating.improvements.joinToString(";")
        )
        return json
    }
}