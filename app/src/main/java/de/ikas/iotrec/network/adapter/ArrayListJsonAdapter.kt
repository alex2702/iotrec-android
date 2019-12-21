package de.ikas.iotrec.network.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Used by Moshi to convert between JSON and Kotlin objects
 */
internal class ArrayListJsonAdapter {
    @ToJson
    fun arrayListToJson(list: ArrayList<String>): List<String> = list

    @FromJson
    fun arrayListFromJson(list: List<String>): ArrayList<String> = ArrayList(list)
}