package de.ikas.iotrec.network.adapter

import android.util.Log
import com.squareup.moshi.FromJson
import de.ikas.iotrec.network.json.WeatherDataJson
import de.ikas.iotrec.network.model.WeatherData

internal class WeatherDataJsonAdapter {
    @FromJson
    fun weatherDataFromJson(weatherDataJson: WeatherDataJson): WeatherData {

        var weather = "-"
        // see API doc for groups: https://openweathermap.org/weather-conditions
        if(weatherDataJson.weather[0].id in 200..202 || weatherDataJson.weather[0].id in 230..232 || weatherDataJson.weather[0].id in 300..531 || weatherDataJson.weather[0].id in 701..781) {
            weather = "RAINY"
        } else if(weatherDataJson.weather[0].id in 210..202 || weatherDataJson.weather[0].id == 221) {
            weather = "WINDY"
        } else if(weatherDataJson.weather[0].id in 600..622) {
            weather = "SNOW"
        } else if(weatherDataJson.weather[0].id in 800..801) {
            weather = "SUNNY"
        } else if(weatherDataJson.weather[0].id in 802..804) {
            weather = "CLOUDY"
        }

        val temperature = weatherDataJson.main.temp.toInt()

        return WeatherData(
            weather,
            temperature
        )
    }
}