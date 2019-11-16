package de.ikas.iotrec.network.adapter

import com.squareup.moshi.ToJson
import com.squareup.moshi.FromJson
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.network.json.CategoryJson
import de.ikas.iotrec.network.json.PreferenceJson


internal class PreferenceJsonAdapter {
    @FromJson
    fun preferenceFromJson(preferenceJson: PreferenceJson): Preference {
        val preference = Preference(
            preferenceJson.id,
            preferenceJson.category,
            preferenceJson.value,
            preferenceJson.user
        )
        return preference
    }

    @ToJson
    fun preferenceToJson(preference: Preference): PreferenceJson {
        val json = PreferenceJson(
            preference.id,
            preference.category,
            preference.value,
            preference.user
        )
        return json
    }
}