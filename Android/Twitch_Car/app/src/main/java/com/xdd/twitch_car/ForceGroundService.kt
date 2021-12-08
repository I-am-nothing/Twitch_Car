package com.xdd.twitch_car

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.lang.StringBuilder
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.schedule
import android.net.wifi.WifiManager
import android.text.format.Formatter
import java.lang.Exception

class ForceGroundService: Service() {

    private var isFirstStart = true

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){
            ACTION_START_FOREGROUND_SERVICE -> {
                if(isFirstStart){
                    isFirstStart = false

                    MySocketServer(applicationContext, SOCKET_SERVER_PORT)

                    createNotificationChannel()

                    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val ip = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
                    val vocabularyIntent = Intent(this, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this, 87, vocabularyIntent, PendingIntent.FLAG_IMMUTABLE)
                    val notification = NotificationCompat.Builder(baseContext, "XDD1")
                        .setContentTitle("Twitch Car Service Is Running!")
                        .setContentText("Twitch car can connect service now, IP:${ip}")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .build()

                    startForeground(187, notification)
                }
            }
            ACTION_STOP_FOREGROUND_SERVICE -> {
                if(!isFirstStart){
                    isFirstStart = true

                    MySocketServer.stopSocketServer()

                    stopForeground(true)
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel("XDD1", "Vocabulary channel", NotificationManager.IMPORTANCE_HIGH)

        val manager = getSystemService(NotificationManager::class.java) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()

        super.onDestroy()
    }

    companion object{
        const val SOCKET_SERVER_PORT = 9700
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    }
}