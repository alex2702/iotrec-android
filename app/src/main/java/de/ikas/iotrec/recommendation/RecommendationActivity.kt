package de.ikas.iotrec.recommendation

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import de.ikas.iotrec.R

class RecommendationActivity : AppCompatActivity() {

    protected val TAG = "RecommendationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(
            TAG,
            "I am onCreate of RecommendationActivitity."
        )
        setContentView(R.layout.activity_recommendation)
    }
}
