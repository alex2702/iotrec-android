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
    }

    @SuppressLint("MissingPermission")
    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        
        app = applicationContext as IotRecApplication
        recommendationRepository = app.recommendationRepository
        thingRepository = app.thingRepository
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // only check if intent exists, because we need the thing stored in it
        if(intent != null) {
            // receive the thing
            val thingId = intent!!.getStringExtra("thingId")

            // create bare recommendation object that can be sent to API
            val bareRecommendation = Recommendation("", thingId, 0f, false, 0, "", 0,
                null, null, 0)

            // make API call
            GlobalScope.launch {
                // get thing belonging to recommendation
                val thing = thingRepository.getThing(bareRecommendation.thing)

                val thingExists = thing != null

                // get experiment data from shared prefs
                val experimentCurrentRun = sharedPreferences.getInt("experimentCurrentRun", 0)
                val experimentCurrentScenario = sharedPreferences.getString("experimentCurrentScenario", "")
                val experimentCurrentStep = sharedPreferences.getString("experimentCurrentStep", "")

                // interpret experiment data into boolean flags
                val experimentIsRunning = experimentCurrentRun > 0 && experimentCurrentScenario != ""
                val experimentTestRunIsActive = experimentIsRunning && experimentCurrentStep == "perform_test_run"

                val thingBelongsToScenario = thingId.endsWith("jobfair") || thingId.endsWith("museum")

                // don't check if thing belongs to a scenario but there is no active experiment test run
                if(thingExists && !(!experimentTestRunIsActive && thingBelongsToScenario)) {
                    // set up some boolean flags with relevant criteria
                    val thingWasFetchedSuccessfullyBefore = thing.lastQueried!!.time > 0
                    val thingWasLastCheckedForRecoGTE10MinutesAgo = thing.lastCheckedForRecommendation!!.time < Date().time - 10 * 60 * 1000
                    val thingWasLastRecommendedGTE24HoursAgo = thing.lastRecommended!!.time < Date().time - 24 * 60 * 60 * 1000
                    val thingWasNeverRecommendedBefore = thing.lastRecommended!!.time == 0L

                    // only check for recommendation if:
                    // we know the thing and
                    // last check was at least 10 minutes ago and
                    // last recommendation (to user) was at least 24 hrs ago (or never recommended)
                    if (thingWasFetchedSuccessfullyBefore && thingWasLastCheckedForRecoGTE10MinutesAgo && (thingWasLastRecommendedGTE24HoursAgo || thingWasNeverRecommendedBefore)) {
                        // only allow one recommendation check for this thing at a time
                        // (prevents starting another request while previous one is still running)
                        if (!thing.recommendationQueryRunning) {
                            // if it's available, get "recommendation query running" lock on thing
                            thingRepository.setRecommendationQueryRunning(thingId, true)

                            // if an experiment is running, create a bare recommendation with the dummy context
                            if(experimentIsRunning) {
                                val currentExperiment = app.experimentRepository.getExperimentByOrder(experimentCurrentRun)
                                bareRecommendation.context_temperature_raw = 10
                                bareRecommendation.context_weather_raw = "CLOUDY"
                                bareRecommendation.context_length_of_trip_raw = 180
                                bareRecommendation.context_time_of_day_raw = null
                                bareRecommendation.context_crowdedness_raw = null
                                if(currentExperiment != null) {
                                    bareRecommendation.experiment = currentExperiment.id
                                }

                            // else create a recommendation object with the real weather, temperature and length of trip
                            } else {
                                // check if weather data is up to date (15mins) and if not, fetch current data
                                val lastWeatherFetch = sharedPreferences.getLong("weather.timestamp", 0L)
                                val weather = sharedPreferences.getString("weather.weather", "")
                                val temperature = sharedPreferences.getInt("weather.temperature", -100)

                                if (Date(lastWeatherFetch).time < Date().time - 15 * 60 * 1000 || weather == "" || temperature == -100) {
                                    // get coordinates
                                    var lat = app.location.latitude
                                    var lon = app.location.longitude

                                    // if location could not be acquired
                                    if (lat == 0.0 || lon == 0.0) {
                                        lat = 48.262529 // default to Garching
                                        lon = 11.668790 // default to Garching
                                    }

                                    val weatherResult = app.openWeatherApi.getWeather(lat, lon)

                                    // if data was fetched successfully, save it to shared prefs
                                    if (weatherResult.isSuccessful) {
                                        val weatherData = weatherResult.body()

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

                                // put context data into bare recommendation
                                bareRecommendation.context_temperature_raw = temperature
                                bareRecommendation.context_weather_raw = weather!!
                                bareRecommendation.context_length_of_trip_raw = 0
                            }

                            try {
                                // make API request
                                val result = app.iotRecApi.createRecommendation(bareRecommendation)

                                if (result.isSuccessful) {
                                    val resultRecommendation = result.body()

                                    if (resultRecommendation != null) {
                                        // insert (full) result recommendation into database
                                        recommendationRepository.insert(resultRecommendation)

                                        // update timestamp of last recommendation check (to now)
                                        thingRepository.updateLastCheckedForRecommendation(
                                            thing.id,
                                            Date()
                                        )

                                        // if reco is to be shown, send notification
                                        if (resultRecommendation.invokeRec) {
                                            showRecommendation(resultRecommendation, thing)

                                            // update timestamp of last recommendation check (to now)
                                            thingRepository.updateLastRecommended(
                                                thing.id,
                                                Date()
                                            )
                                        }
                                    } else {
                                        Log.e(TAG, "result recommendation was null")
                                    }
                                } else {
                                    Log.e(TAG, "Error when getting recommendation: $result")
                                }
                            } catch (e: Throwable) {
                                Log.e(TAG, e.toString())
                            }

                            // release "recommendation query running" lock on thing
                            thingRepository.setRecommendationQueryRunning(thingId, false)
                        }
                    } else {
                        //only debug logs
                        if(!thingWasFetchedSuccessfullyBefore) {
                            Log.d(TAG, "NOT CHECKING FOR RECOMMENDATION FOR ${thing.id} – ${thing.title}: thing was not fetched")
                        }

                        if(!thingWasLastCheckedForRecoGTE10MinutesAgo) {
                            Log.d(TAG, "NOT CHECKING FOR RECOMMENDATION FOR ${thing.id} – ${thing.title}: thing was last check for a reco less than 10 minutes ago")
                        }

                        if(!(thingWasLastRecommendedGTE24HoursAgo || thingWasNeverRecommendedBefore)) {
                            Log.d(TAG, "NOT CHECKING FOR RECOMMENDATION FOR ${thing.id} – ${thing.title}: thing was recommeded too recently")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun showRecommendation(recommendation: Recommendation, thing: Thing) {
        Log.d(TAG, "showRecommendation")

        // Add as notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // put ReocommendationActivity into intent
        val notificationIntent = Intent(this, RecommendationActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // send along the recommended thing and the recommendation
        var thingBundle = Bundle()
        thingBundle.putParcelable("thing", thing)
        notificationIntent.putExtra("thingBundle", thingBundle)

        var recommendationBundle = Bundle()
        recommendationBundle.putParcelable("recommendation", recommendation)
        notificationIntent.putExtra("recommendationBundle", recommendationBundle)

        val pendingIntent = PendingIntent.getActivity(this, notificationRequestCode++, notificationIntent, PendingIntent.FLAG_ONE_SHOT)

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // buld notification
        val builder = NotificationCompat.Builder(this, TAG)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(thing.title)
            .setContentText("I found something you should check out!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setSound(sound)
            .setGroup("IOTREC_RECOMMENDATION_GROUP")

        // on higher Android versions, create a notification channel
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

        // send notification
        notificationManager.notify(Random.nextInt(10000000), notification)
    }
}
