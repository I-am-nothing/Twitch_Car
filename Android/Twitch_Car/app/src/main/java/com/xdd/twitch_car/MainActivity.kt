package com.xdd.twitch_car

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.xdd.twitch_car.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var joyStick: JoyStick

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(android.R.attr.windowFullscreen, android.R.attr.windowFullscreen)
        setContentView(binding.root)

        joyStick = JoyStick(275f, 700f, 70f, 40f)

        binding.button.setOnClickListener {
            if(binding.button.text == "Start Server"){
                intent = Intent(this, ForceGroundService::class.java)
                intent.action = ForceGroundService.ACTION_START_FOREGROUND_SERVICE
                startForegroundService(intent)
                binding.button.text = "Stop Server"

                MySocketServer.checkSocketServerStart{
                    MySocketServer(applicationContext).deviceAcceptListener {
                        var i = 0
                        Timer().schedule(100, 1000){
                            it.sendMessage(i.toString())
                            i ++
                        }
                        it.onMessageInputListener({

                        }, { image ->
                            runOnUiThread {
                                binding.imageView.setImageBitmap(image)
                            }
                        })
                        runOnUiThread {
                            Toast.makeText(this, "Client connected : ${it.getIpAddress()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                intent = Intent(this, ForceGroundService::class.java)
                intent.action = ForceGroundService.ACTION_STOP_FOREGROUND_SERVICE
                startForegroundService(intent)
                binding.button.text = "Start Server"
            }
        }

    }

    override fun onDestroy() {
        intent = Intent(this, ForceGroundService::class.java)
        intent.action = ForceGroundService.ACTION_STOP_FOREGROUND_SERVICE
        startForegroundService(intent)
        super.onDestroy()
    }
}