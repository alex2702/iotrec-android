package de.ikas.iotrec.recommendation

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.database.repository.RecommendationRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import de.ikas.iotrec.R
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.ThingRepository
import java.util.*
import android.media.RingtoneManager
import android.preference.PreferenceManager
import kotlin.random.Random


class RecommendationCheckerService : Service() {

    private val TAG = "RecoCheckerService"

    lateinit var app: IotRecApplication
    private lateinit var recommendationRepository: RecommendationRepository
    private lateinit var thingRepository: ThingRepository
    private var notificationRequestCode = 0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")
    }

    @SuppressLint("MissingPermission")
    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)

        Log.d(TAG, "onStart")

        app = applicationContext as IotRecApplication
        recommendationRepository = app.recommendationRepository
        thingRepository = app.thingRepository
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if(intent != null) {
            val thingId = intent!!.getStringExtra("thingId")

            val bareRecommendation = Recommendation("", thingId, 0f, false, 0, "", 0)

            // make API call
            GlobalScope.launch {
                // get thing belonging to recommendation
                val thing = thingRepository.getThing(bareRecommendation.thing)
                if(thing != null) {
                    //only check for recommendation if we know the thing and if last check was at least 5 minutes ago and if last recommendation (to user) was at least 7 days ago (or never recommended)
                    if (thing.lastQueried!!.time > 0 && thing.lastCheckedForRecommendation!!.time < Date().time - 5 * 60 * 1000 && (thing.lastRecommended!!.time < Date().time - 8 * 60 * 60 * 1000 || thing.lastRecommended!!.time == 0L)) {    // changed to 8hrs for testing
                        if (!thing.recommendationQueryRunning) {
                            Log.d(TAG, "Getting recommendation for " + thing.id.toString())

                            // if it's available, get "recommendation query running" lock on thing
                            thingRepository.setRecommendationQueryRunning(thingId, true)

                            // check if weather data is up to date (15mins) and if not, fetch current data
                            val lastWeatherFetch =
                                sharedPreferences.getLong("weather.timestamp", 0L)
                            val weather = sharedPreferences.getString("weather.weather", "")
                            val temperature =
                                sharedPreferences.getInt("weather.temperature", -100)

                            if (Date(lastWeatherFetch).time < Date().time - 15 * 60 * 1000 || weather == "" || temperature == -100) {
                                // get coordinates
                                var lat = app.location.latitude
                                var lon = app.location.longitude
                                if (lat == 0.0 || lon == 0.0) {
                                    lat = 48.262529 // Garching
                                    lon = 11.668790 // Garching
                                }

                                Log.d(
                                    TAG,
                                    "using location(lat = " + lat.toString() + "; lon = " + lon.toString()
                                )

                                val weatherResult = app.openWeatherApi.getWeather(lat, lon)

                                if (weatherResult.isSuccessful) {
                                    val weatherData = weatherResult.body()
                                    Log.d(TAG, weatherData.toString())
                                    val editor = sharedPreferences.edit()
                                    editor.putLong("weather.timestamp", Date().time)
                                    editor.putString(
                                        "weather.weather",
                                        weatherData!!.description
                                    )
                                    editor.putInt(
                                        "weather.temperature",
                                        weatherData.temperature
                                    )
                                    editor.apply()
                                }
                            }

                            bareRecommendation.context_temperature_raw = temperature
                            bareRecommendation.context_weather_raw = weather!!
                            bareRecommendation.context_length_of_trip_raw = 0

                            try {
                                //val result = app.iotRecApi.createRecommendation(thingId)
                                val result =
                                    app.iotRecApi.createRecommendation(bareRecommendation)

                                if (result.isSuccessful) {
                                    val resultRecommendation = result.body()
                                    Log.d(TAG, resultRecommendation.toString())

                                    if (resultRecommendation != null) {
                                        recommendationRepository.insert(resultRecommendation)

                                        // update timestamp of last recommendation check (to now)
                                        thingRepository.updateLastCheckedForRecommendation(
                                            thing.id,
                                            Date()
                                        )

                                        // if reco is to be shown, send notification
                                        if (resultRecommendation.invokeRec) {
                                            Log.d(TAG, "showing reco")
                                            showRecommendation(resultRecommendation, thing)

                                            // update timestamp of last recommendation check (to now)
                                            thingRepository.updateLastRecommended(
                                                thing.id,
                                                Date()
                                            )
                                        }
                                    }
                                } else {
                                    Log.i(TAG, "Error when getting recommendation: $result")
                                }
                            } catch (e: Throwable) {
                                Log.d(TAG, e.toString())
                            }

                            var updatedThing = thingRepository.getThing(bareRecommendation.thing)
                            Log.d(TAG, "Full thing after reco: " + updatedThing.toString())

                            // release "recommendation query running" lock on thing
                            thingRepository.setRecommendationQueryRunning(thingId, false)
                        } else {
                            Log.d(
                                TAG,
                                "Not checking for recommendation for ${thing.id} because lock is taken"
                            )
                        }
                    } else {
                        Log.d(
                            TAG,
                            "Not checking for recommendation for ${thing.id} because we don't have the details or last check or last recommendation was too recent"
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")

        TODO("Return the communication channel to the service.")
    }

    private fun showRecommendation(recommendation: Recommendation, thing: Thing) {
        Log.d(TAG, "showRecommendation")

        // Add as notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, RecommendationActivity::class.java)
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        var thingBundle = Bundle()
        thingBundle.putParcelable("thing", thing)
        notificationIntent.putExtra("thingBundle", thingBundle)

        var recommendationBundle = Bundle()
        recommendationBundle.putParcelable("recommendation", recommendation)
        notificationIntent.putExtra("recommendationBundle", recommendationBundle)

        val pendingIntent = PendingIntent.getActivity(this, notificationRequestCode++, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, TAG)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(thing.title)
            .setContentText("I found something you should check out!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setSound(sound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "IotRec Notification Channel ID",
                "IotRec Recommendation Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "IotRec Notification Channel Description"

            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channel.id)
        }

        val notification = builder.build()

        notificationManager.notify(Random.nextInt(10000000), notification)
    }
}
