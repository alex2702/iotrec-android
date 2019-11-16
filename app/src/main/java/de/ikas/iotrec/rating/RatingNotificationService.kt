package de.ikas.iotrec.rating

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import de.ikas.iotrec.R
import de.ikas.iotrec.database.model.Feedback
import de.ikas.iotrec.database.model.Thing
import de.ikas.iotrec.recommendation.RatingActivity
import kotlin.random.Random

class RatingNotificationService: IntentService("RatingNotificationService") {

    protected val TAG = "RatingNotificationServi"

    private var notificationRequestCode = 0

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "IotRec Rating Notification Channel"
        const val NOTIFICATION_CHANNEL_NAME = "IotRec Rating Notification"
    }

    private fun createNotificationChannel() {
        //only needs to be created on higher API versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "IotRec Rating Notification Channel Description"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        createNotificationChannel()

        if (intent != null) {
            val notificationTime = intent.getLongExtra("notificationTime", 0L)
            val feedbackBundle = intent.getBundleExtra("feedback")
            val feedback = feedbackBundle.getParcelable("feedback") as Feedback
            val thingBundle = intent.getBundleExtra("thing")
            val thing = thingBundle.getParcelable("thing") as Thing
            val recommendationBundle = intent.getBundleExtra("recommendation")

            if (notificationTime > 0) {
                var notificationTitle = ""
                var notificationBody = ""
                if(feedback.value > 0) {
                    notificationTitle = "How did you like ${thing.title}?"
                    notificationBody = "You have been recommended ${thing.title} recently. How did you like it?"
                } else if(feedback.value < 0) {
                    notificationTitle = "Why did you reject ${thing.title}?"
                    notificationBody = "You have been recommended ${thing.title} recently but rejected it. Could we get some feedback?"
                }

                val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notificationIntent = Intent(this, RatingActivity::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                notificationIntent.putExtra("thing", thingBundle)
                notificationIntent.putExtra("recommendation", recommendationBundle)
                notificationIntent.putExtra("feedback", feedbackBundle)

                val pendingIntent = PendingIntent.getActivity(this, notificationRequestCode++, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val builder = NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID
                )
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationBody)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
                    .setSound(sound)
                    .setGroup("IOTREC_RATING_GROUP")

                val notification = NotificationCompat.BigTextStyle(builder).bigText(notificationBody).build()

                notificationManager.notify(Random.nextInt(10000000), notification)
            }
        }
    }
}