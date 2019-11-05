package de.ikas.iotrec.database.util

import android.util.Log
import androidx.room.TypeConverter

object ArrayListConverter {
    @TypeConverter
    @JvmStatic
    fun toList(value: String): ArrayList<String> {
        Log.d("ArrayListConverter", "toList: " + value)
        val arrayList = arrayListOf<String>()
        arrayList.addAll(value.let { it.split(";").map { x -> x.trim() } })
        return arrayList
    }

    @TypeConverter
    @JvmStatic
    fun fromList(list: ArrayList<String>): String {
        Log.d("ArrayListConverter", "fromList: " + list.toString())
        return list.joinToString(";")
    }
}