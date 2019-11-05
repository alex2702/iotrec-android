package de.ikas.iotrec.network.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal class ArrayListJsonAdapter {
    @ToJson
    fun arrayListToJson(list: ArrayList<String>): List<String> = list

    @FromJson
    fun arrayListFromJson(list: List<String>): ArrayList<String> = ArrayList(list)
}