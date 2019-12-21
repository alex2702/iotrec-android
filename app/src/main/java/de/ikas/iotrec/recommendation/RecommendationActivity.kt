package de.ikas.iotrec.recommendation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Feedback
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.ThingRepository
import de.ikas.iotrec.network.model.AnalyticsEvent
import de.ikas.iotrec.rating.RatingAlarmReceiver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class RecommendationActivity : AppCompatActivity() {

    protected val TAG = "RecommendationActivity"

    private lateinit var app: IotRecApplication
    private lateinit var thingRepository: ThingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as IotRecApplication
        thingRepository = app.thingRepository

        // make it a full-screen activitiy
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_recommendation)

        // get the thing object from intent
        val thingBundle = intent.getBundleExtra("thingBundle")
        val thing: Thing = thingBundle.getParcelable("thing") as Thing

        // get the recommendation object from intent
        val recommendationBundle = intent.getBundleExtra("recommendationBundle")
        val recommendation: Recommendation = recommendationBundle.getParcelable("recommendation") as Recommendation

        // get UI elements
        val thingImage: ImageView = findViewById(R.id.thing_image)
        val thingTitle: TextView = findViewById(R.id.thing_title)
        val thingDescription: TextView = findViewById(R.id.thing_description)
        val thingDistance: TextView = findViewById(R.id.thing_distance)
        val acceptButton = findViewById<Button>(R.id.reco_accept)
        val rejectButton = findViewById<Button>(R.id.reco_reject)
        val explanationButton = findViewById<Button>(R.id.button_explanation)

        thingTitle.text = thing.title
        thingDescription.text = thing.description
        thingDistance.text = "%.2f".format(thing.distance)

        if(thing.image != "") {
            Picasso.get().load(thing.image).into(thingImage)
        }

        // timestamps that are later used to prevent double clicks
        var lastClickTimeAccept = 0L
        var lastClickTimeReject = 0L
        var lastClickTimeExpl = 0L

        acceptButton.setOnClickListener {
            //only allow one button click per 5 seconds
            if(SystemClock.elapsedRealtime() - lastClickTimeAccept < 5000) return@setOnClickListener
            lastClickTimeAccept = SystemClock.elapsedRealtime()

            // create bare feedback object for API
            val bareFeedback = Feedback("", 1, recommendation.id)

            GlobalScope.launch {
                // create feedback object
                try {
                    // send feedback object to API
                    val result = app.iotRecApi.createFeedback(recommendation.id, bareFeedback)

                    if (result.isSuccessful) {
                        // if feedback was sent, schedule the Rating notification
                        scheduleRatingNotification(result.body()!!, recommendation, thing)

                        runOnUiThread {
                            Toast.makeText(
                                this@RecommendationActivity,
                                "Have fun!",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // end activity
                        finish()
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@RecommendationActivity,
                                "Could not send feedback: ${result.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Throwable) {
                    Log.d(TAG, e.toString())
                }
            }
        }

        rejectButton.setOnClickListener {
            //only allow one button click per 5 seconds
            if(SystemClock.elapsedRealtime() - lastClickTimeReject < 5000) return@setOnClickListener
            lastClickTimeReject = SystemClock.elapsedRealtime()

            // create bare feedback object for API
            val bareFeedback = Feedback("", -1, recommendation.id)

            GlobalScope.launch {
                // create feedback object
                try {
                    // send feedback object to API
                    val result = app.iotRecApi.createFeedback(recommendation.id, bareFeedback)

                    if (result.isSuccessful) {
                        // if feedback was sent, schedule the Rating notification
                        scheduleRatingNotification(result.body()!!, recommendation, thing)

                        runOnUiThread {
                            Toast.makeText(
                                this@RecommendationActivity,
                                "Maybe later...",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // end activity
                        finish()
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@RecommendationActivity,
                                "Could not send feedback: ${result.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Throwable) {
                    Log.d(TAG, e.toString())
                }
            }
        }

        explanationButton.setOnClickListener {
            //only allow one button click per 500 seconds (so users can only click once)
            if(SystemClock.elapsedRealtime() - lastClickTimeExpl < 500000) return@setOnClickListener
            lastClickTimeExpl = SystemClock.elapsedRealtime()

            // create analyticsEvent object for API
            val analyticsEvent = AnalyticsEvent("RECO_EXPL", recommendation.id, thing.id, 0f)

            GlobalScope.launch {
                // create analytics object
                try {
                    // send to API
                    val result = app.iotRecApi.createAnalyticsEvent(analyticsEvent)

                    runOnUiThread {
                        Toast.makeText(
                            this@RecommendationActivity,
                            "Sorry, I can't tell you yet.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Throwable) {
                    Log.d(TAG, e.toString())
                }
            }
        }
    }

    private fun scheduleRatingNotification(feedback: Feedback, recommendation: Recommendation, thing: Thing) {
        // get notification time (5 minutes from now)
        val notificationTime = Calendar.getInstance().timeInMillis + 5 * 60 * 1000

        // create an alarm manager and include an intent bound to the RatingAlarmReceiver class
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(applicationContext, RatingAlarmReceiver::class.java)
        alarmIntent.putExtra("notificationTime", notificationTime)

        // put the recommended thing in the intent
        val thingBundle = Bundle()
        thingBundle.putParcelable("thing", thing)
        alarmIntent.putExtra("thing", thingBundle)

        // put the feedback that was made in the intent
        val feedbackBundle = Bundle()
        feedbackBundle.putParcelable("feedback", feedback)
        alarmIntent.putExtra("feedback", feedbackBundle)

        // put the recommendation that was shown in the bundle
        val recommendationBundle = Bundle()
        recommendationBundle.putParcelable("recommendation", recommendation)
        alarmIntent.putExtra("recommendation", recommendationBundle)

        val pendingIntent = PendingIntent.getBroadcast(this, Random.nextInt(10000000), alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
    }
}