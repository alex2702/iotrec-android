package de.ikas.iotrec.network

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.ikas.iotrec.account.data.model.LoggedInUser
import de.ikas.iotrec.network.json.WeatherDataJson
import de.ikas.iotrec.network.model.WeatherData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import com.squareup.moshi.JsonAdapter
import de.ikas.iotrec.network.adapter.WeatherDataJsonAdapter


class OpenWeatherApiInit(context: Context) {

    private val TAG = "OpenWeatherApiInit"

    private val openWeatherApi: OpenWeatherApi
    private val moshi: Moshi

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val openWeatherApiClient = OkHttpClient().newBuilder()
            .build()

        moshi = Moshi.Builder()
            .add(WeatherDataJsonAdapter())
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .client(openWeatherApiClient)
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        openWeatherApi = retrofit.create(OpenWeatherApi::class.java)
    }

    suspend fun getWeather(lat: Double, lon: Double): Response<WeatherData> {
        //val jsonAdapter = moshi.adapter(WeatherData::class.java)
        //val weatherData = jsonAdapter.fromJson()

        return openWeatherApi.getWeather(lat, lon)
    }
}