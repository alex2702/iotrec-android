package de.ikas.iotrec.network.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.jetbrains.annotations.Nullable

class DefaultStringAdapter {
    @FromJson
    fun fromJson(@Nullable jsonString: String?): String {
        return jsonString ?: ""
    }

    @ToJson
    fun toJson(jsonString: String?) = jsonString
}