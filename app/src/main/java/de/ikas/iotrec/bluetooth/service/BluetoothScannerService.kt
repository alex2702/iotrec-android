package de.ikas.iotrec.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import de.ikas.iotrec.R
import android.app.Notification
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import android.os.RemoteException
import org.altbeacon.beacon.*


class BluetoothScannerService : Service(), BeaconConsumer {

    private lateinit var beaconManager: BeaconManager

    companion object {
        private val TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE"
        val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        val ACTION_PAUSE = "ACTION_PAUSE"
        val ACTION_PLAY = "ACTION_PLAY"
    }


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG_FOREGROUND_SERVICE, "My foreground service onCreate().")

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        beaconManager.bind(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        beaconManager.unbind(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action

            when (action) {
                ACTION_START_FOREGROUND_SERVICE -> {
                    startForegroundService()
                    Toast.makeText(applicationContext, "Foreground service is started.", Toast.LENGTH_LONG).show()
                }
                ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopForegroundService()
                    Toast.makeText(applicationContext, "Foreground service is stopped.", Toast.LENGTH_LONG).show()
                }
                ACTION_PLAY -> Toast.makeText(applicationContext, "You click Play button.", Toast.LENGTH_LONG).show()
                ACTION_PAUSE -> Toast.makeText(applicationContext, "You click Pause button.", Toast.LENGTH_LONG).show()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /* Used to build and start foreground service. */
    private fun startForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Start foreground service.")

        // Create notification default intent.
        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        // Create notification builder.
        val builder = NotificationCompat.Builder(this)

        // Make notification show big text.
        val bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle.setBigContentTitle("IoT Recommendations")
        bigTextStyle.bigText("IoT Rec is scanning for Bluetooth beacons.")
        // Set big text style.
        builder.setStyle(bigTextStyle)

        builder.setWhen(System.currentTimeMillis())
        builder.setSmallIcon(R.mipmap.ic_launcher)
        val largeIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_home_black_24dp)
        builder.setLargeIcon(largeIconBitmap)
        // Make the notification max priority.
        builder.setPriority(Notification.PRIORITY_MAX)
        // Make head-up notification.
        //builder.setFullScreenIntent(pendingIntent, true)

        // Add Play button intent in notification.
        //val playIntent = Intent(this, BluetoothScannerService::class.java)
        //playIntent.action = ACTION_PLAY
        //val pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0)
        //val playAction = NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pendingPlayIntent)
        //builder.addAction(playAction)

        // Add Pause button intent in notification.
        //val pauseIntent = Intent(this, BluetoothScannerService::class.java)
        //pauseIntent.action = ACTION_PAUSE
        //val pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0)
        //val prevAction = NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingPrevIntent)
        //builder.addAction(prevAction)

        // Build the notification.
        val notification = builder.build()

        // Start foreground service.
        startForeground(1, notification)
    }

    private fun stopForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.")

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }


    override fun onBeaconServiceConnect() {
        beaconManager.removeAllMonitorNotifiers()

        beaconManager.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region) {
                Log.i(TAG_FOREGROUND_SERVICE, "I just saw an beacon for the first time!")
            }

            override fun didExitRegion(region: Region) {
                Log.i(TAG_FOREGROUND_SERVICE, "I no longer see an beacon")
            }

            override fun didDetermineStateForRegion(state: Int, region: Region) {
                Log.i(TAG_FOREGROUND_SERVICE, "I have just switched from seeing/not seeing beacons: $state")
            }
        })

        try {
            beaconManager.startMonitoringBeaconsInRegion(Region("myMonitoringUniqueId", null, null, null))
        } catch (e: RemoteException) {

        }
    }
}
