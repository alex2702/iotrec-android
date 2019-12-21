package de.ikas.iotrec.network

import de.ikas.iotrec.network.model.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenWeatherApi {
    //TODO insert openweathermap app ID
    @GET("weather?APPID=<APPID>&units=metric")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<WeatherData>
}