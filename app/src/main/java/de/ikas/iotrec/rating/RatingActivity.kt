package de.ikas.iotrec.recommendation

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Feedback
import de.ikas.iotrec.database.model.Rating
import de.ikas.iotrec.database.model.Recommendation
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.database.repository.ThingRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RatingActivity : AppCompatActivity() {

    protected val TAG = "RatingActivity"

    private lateinit var app: IotRecApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as IotRecApplication

        // make it a full-screen activitiy
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        setContentView(R.layout.activity_rating)

        // get the objects from intent
        val thingBundle = intent.getBundleExtra("thing")
        val thing: Thing = thingBundle.getParcelable("thing") as Thing
        val recommendationBundle = intent.getBundleExtra("recommendation")
        val recommendation: Recommendation = recommendationBundle.getParcelable("recommendation") as Recommendation
        val feedbackBundle = intent.getBundleExtra("feedback")
        val feedback: Feedback = feedbackBundle.getParcelable("feedback") as Feedback

        // get and populate UI elements
        val thingTitle: TextView = findViewById(R.id.intro_body_1)
        thingTitle.text = "You have recently been recommended ${thing.title}."

        val textBody: TextView = findViewById(R.id.intro_body_2)

        if(feedback.value < 0) {
            textBody.text = "You have rejected the recommendation. Care to rate the recommendation anyway?\nDid it not fit your interests? Was it inappropriate at the time? " +
                    "Was it just not useful to you?\n\nPlease select a rating between 0 and 5."
        }

        val sendButton = findViewById<Button>(R.id.send_button)

        val ratingBar: RatingBar = findViewById(R.id.rating_bar)

        // timestamp to prevent double clicks
        var lastClickTime = 0L

        sendButton.setOnClickListener {
            //only allow one button click per 5 seconds
            if(SystemClock.elapsedRealtime() - lastClickTime < 5000) return@setOnClickListener
            lastClickTime = SystemClock.elapsedRealtime()

            // get rating
            val ratingValue = ratingBar.rating

            // bare rating object for API
            val bareRating = Rating("", ratingValue, arrayListOf(), recommendation.id)

            GlobalScope.launch {
                // create rating object
                try {
                    // send to API
                    val result = app.iotRecApi.createRating(recommendation.id, bareRating)

                    if (result.isSuccessful) {
                        // show a thank you toast
                        runOnUiThread {
                            Toast.makeText(
                                this@RatingActivity,
                                "Your rating was submitted. Thank you!",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // end activity
                        finish()
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@RatingActivity,
                                "Could not send rating: ${result.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Throwable) {
                    Log.d(TAG, e.toString())
                }
            }
        }
    }
}