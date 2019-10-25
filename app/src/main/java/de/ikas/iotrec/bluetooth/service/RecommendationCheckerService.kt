package de.ikas.iotrec.bluetooth.service

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
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import de.ikas.iotrec.R
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.ThingRepository
import de.ikas.iotrec.recommendation.RecommendationActivity
import java.util.*
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.RingtoneManager
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.Criteria
import android.preference.PreferenceManager
import java.lang.Exception


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

        app = applicationContext as IotRecApplication
        recommendationRepository = app.recommendationRepository
        thingRepository = app.thingRepository
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        if(intent != null) {
            val thingId = intent!!.getStringExtra("thingId")
            val bareRecommendation = Recommendation("", thingId, 0f, false)

            // make API call
            GlobalScope.launch {
                // get thing belonging to recommendation
                val thing = thingRepository.getThing(bareRecommendation.thing)
                if(thing != null) {
                    Log.d(TAG, thing.toString())
                    //only check for recommendation if we know the thing and if last check was at least 10 minutes ago and if last recommendation (to user) was at least 7 days ago
                    if(thing.lastQueried!!.time > 0 /*&& (thing.lastCheckedForRecommendation!!.time < Date().time - 10 * 60 * 1000 || thing.lastRecommended!!.time < Date().time - 7 * 24 * 60 * 60 * 1000)*/) {
                        // check if weather data is up to date (15mins) and if not, fetch current data
                        val lastWeatherFetch = sharedPreferences.getLong("weather.timestamp", 0L)
                        val weather = sharedPreferences.getString("weather.weather", "")
                        val temperature = sharedPreferences.getInt("weather.temperature", -100)

                        if(Date(lastWeatherFetch).time < Date().time - 15 * 60 * 1000 || weather == "" || temperature == -100) {
                            // get coordinates
                            val criteria = Criteria()
                            val bestLocationProvider = app.locationManager.getBestProvider(criteria, false)
                            val location = app.locationManager.getLastKnownLocation(bestLocationProvider)
                            var lat = 48.262529 // Garching
                            var lon = 11.668790 // Garching
                            if(location != null) {
                                lat = location.latitude
                                lon = location.longitude
                            }

                            val weatherResult = app.openWeatherApi.getWeather(lat, lon)

                            if (weatherResult.isSuccessful) {
                                val weatherData = weatherResult.body()
                                Log.d(TAG, weatherData.toString())
                                val editor = sharedPreferences.edit()
                                editor.putLong("weather.timestamp", Date().time)
                                editor.putString("weather.weather", weatherData!!.description)
                                editor.putInt("weather.temperature", weatherData.temperature)
                                editor.apply()
                            }
                        }

                        try {
                            //val result = app.iotRecApi.createRecommendation(thingId)
                            val result = app.iotRecApi.createRecommendation(bareRecommendation)

                            if (result.isSuccessful) {
                                val resultRecommendation = result.body()
                                Log.d(TAG, resultRecommendation.toString())

                                if (resultRecommendation != null) {
                                    recommendationRepository.insert(resultRecommendation)

                                    // update timestamp of last recommendation check (to now)
                                    thingRepository.updateLastCheckedForRecommendation(thing.id, Date())

                                    // if reco is to be shown, send notification
                                    if (resultRecommendation.invokeRec) {
                                        Log.d(TAG, "showing reco")
                                        showRecommendation(resultRecommendation, thing)

                                        // update timestamp of last recommendation check (to now)
                                        thingRepository.updateLastRecommended(thing.id, Date())
                                    }
                                }
                            } else {
                                Log.i(TAG, "Error when getting recommendation: $result")
                            }
                        } catch (e: Throwable) {
                            Log.d(TAG, e.toString())
                        }
                    } else {
                        Log.d(TAG, "Not checking for recommendation for ${thing.id} because we don't have the details or last check or last recommendation was too recent")
                    }
                } else {
                    Log.i(TAG, "Thing is null")
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

        notificationManager.notify(0, notification)
    }
}
