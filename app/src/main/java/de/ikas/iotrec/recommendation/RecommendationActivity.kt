package de.ikas.iotrec.recommendation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Feedback
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.ThingRepository
import de.ikas.iotrec.extensions.hideKeyboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecommendationActivity : AppCompatActivity() {

    protected val TAG = "RecommendationActivity"

    private lateinit var app: IotRecApplication
    private lateinit var thingRepository: ThingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as IotRecApplication
        thingRepository = app.thingRepository

        Log.i(
            TAG,
            "I am onCreate of RecommendationActivitity."
        )

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_recommendation)

        // get the thing object from intent
        val thingBundle = intent.getBundleExtra("thingBundle")
        val thing: Thing = thingBundle.getParcelable("thing") as Thing

        // get the recommendation object from intent
        val recommendationBundle = intent.getBundleExtra("recommendationBundle")
        val recommendation: Recommendation = recommendationBundle.getParcelable("recommendation") as Recommendation

        /*
        var thingInDatabase: LiveData<Thing>
        GlobalScope.launch {
            thingInDatabase = thingRepository.getThingLive(thing.id)
        }

        thingInDatabase.observe(this, Observer { user: User? ->

        });
        */

        val thingImage: ImageView = findViewById(R.id.thing_image)
        val thingTitle: TextView = findViewById(R.id.thing_title)
        val thingDescription: TextView = findViewById(R.id.thing_description)
        val thingDistance: TextView = findViewById(R.id.thing_distance)

        val acceptButton = findViewById<Button>(R.id.reco_accept)
        val rejectButton = findViewById<Button>(R.id.reco_reject)

        thingTitle.text = thing.title
        thingDescription.text = thing.description
        thingDistance.text = "%.2f".format(thing.distance)


        acceptButton.setOnClickListener {
            val bareFeedback = Feedback("", 1, recommendation.id)

            GlobalScope.launch {
                // create feedback object
                try {
                    val result = app.iotRecApi.createFeedback(recommendation.id, bareFeedback)

                    Log.d(TAG, result.toString())

                    if (result.isSuccessful) {
                        // end activity
                        finish()
                    } else {
                        Toast.makeText(
                            this@RecommendationActivity,
                            "Could not send feedback: ${result.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Throwable) {
                    Log.d(TAG, e.toString())
                }
            }
        }

        rejectButton.setOnClickListener {
            val bareFeedback = Feedback("", -1, recommendation.id)
            Log.d(TAG, bareFeedback.toString())

            GlobalScope.launch {
                // create feedback object
                try {
                    val result = app.iotRecApi.createFeedback(recommendation.id, bareFeedback)

                    Log.d(TAG, result.toString())

                    if (result.isSuccessful) {
                        // end activity
                        finish()
                    } else {
                        Toast.makeText(
                            this@RecommendationActivity,
                            "Could not send feedback: ${result.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Throwable) {
                    Log.d(TAG, e.toString())
                }
            }
        }
    }
}
