package de.ikas.iotrec.rating

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.ikas.iotrec.database.model.Thing

class RatingAlarmReceiver: BroadcastReceiver() {
    protected val TAG = "RatingAlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "received alarm")

        val service = Intent(context, RatingNotificationService::class.java)

        // put relevant data (timestamp, thing, recommendation, feedback) in the RatingNotificationService intent
        service.putExtra("notificationTime", intent.getLongExtra("notificationTime", 0))

        val thingBundle = intent.getBundleExtra("thing")
        service.putExtra("thing", thingBundle)

        val recommendationBundle = intent.getBundleExtra("recommendation")
        service.putExtra("recommendation", recommendationBundle)

        val feedbackBundle = intent.getBundleExtra("feedback")
        service.putExtra("feedback", feedbackBundle)

        context.startService(service)
    }
}