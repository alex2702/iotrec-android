package de.ikas.iotrec.network

import de.ikas.iotrec.network.model.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenWeatherApi {
    @GET("weather?APPID=1a82f72312153439cb15bd915d7ea544&units=metric")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<WeatherData>
}