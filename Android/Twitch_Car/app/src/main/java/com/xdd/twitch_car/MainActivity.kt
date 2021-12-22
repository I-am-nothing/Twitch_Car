package com.xdd.twitch_car

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.xdd.twitch_car.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity(), JoyStick.JoyStickListener {

    private lateinit var binding: ActivityMainBinding
    private var socketStatus = false
    private var motorClient: MyClient? = null
    private var cameraClient: MyClient? = null
    private var motorDelay = 0L
    private var motorDisplay = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setFlags(android.R.style.Theme_NoTitleBar_Fullscreen, android.R.style.Theme_NoTitleBar_Fullscreen)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.root)

        ViewCompat.getWindowInsetsController(window.decorView)?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        Timer().schedule(500, 1000){
            if(motorDelay != 0L && System.currentTimeMillis() - motorDelay > 5000L){
                motorConnected(false)
                motorDelay = 0L
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "MOTOR DISCONNECTED", Toast.LENGTH_SHORT).show()
                }
            }
        }

        motorConnected(false)
        cameraConnected(false)
        socketStart(false)

        binding.powerBtn.setOnLongClickListener{
            if(socketStatus){
                socketStatus = false
                motorConnected(false)
                cameraConnected(false)
                it.setBackgroundResource(R.drawable.btn_power_red_background)

                intent = Intent(this, ForceGroundService::class.java)
                intent.action = ForceGroundService.ACTION_STOP_FOREGROUND_SERVICE
                startForegroundService(intent)
            }
            else{
                socketStatus = true
                it.setBackgroundResource(R.drawable.btn_power_green_background)

                intent = Intent(this, ForceGroundService::class.java)
                intent.action = ForceGroundService.ACTION_START_FOREGROUND_SERVICE
                startForegroundService(intent)

                MySocketServer(this).deviceAcceptListener { client ->
                    when(client.who){
                        "MOTOR" -> {
                            motorClient = client

                            runOnUiThread{
                                motorConnected(true)
                                Toast.makeText(this, "MOTOR CONNECTED", Toast.LENGTH_SHORT).show()
                            }

                            client.onMessageInputListener({ message ->
                                if(message == "MOTOR OK\n"){
                                    runOnUiThread {
                                        if(motorDisplay){
                                            motorDisplay = false
                                            binding.motorMs.text = "${System.currentTimeMillis() - motorDelay} ms"
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                motorDisplay = true
                                            }, 500)
                                        }
                                        motorDelay = 0
                                    }
                                }
                            }, {

                            })
                        }
                        "CAMERA" -> {
                            runOnUiThread{
                                cameraConnected(true)
                                Toast.makeText(this, "CAMERA CONNECTED", Toast.LENGTH_SHORT).show()
                            }
                            cameraClient = client

                            client.onMessageInputListener({

                            }, { image ->
                                binding.cameraView.setImageBitmap(image)
                            })

                            Thread{
                                client.startCamera()
                            }.start()
                        }
                    }
                }
            }
            socketStart(socketStatus)
            true
        }

        binding.flashBtn.setOnTouchListener { v, event ->

            if(cameraClient != null){
                Thread{
                    when(event.action){
                        MotionEvent.ACTION_DOWN -> {
                            cameraClient!!.startFlashLight()
                        }
                        MotionEvent.ACTION_UP -> {
                            cameraClient!!.stopFlashLight()
                        }
                        MotionEvent.ACTION_BUTTON_RELEASE -> {
                            cameraClient!!.stopFlashLight()
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            cameraClient!!.stopFlashLight()
                        }
                    }
                }.start()
            }

            false
        }
    }

    private fun motorConnected(status: Boolean){
        binding.mainJs.isEnabled = status
        if(status){
            binding.motorLight.setBackgroundResource(R.drawable.light_green)
        }
        else{
            binding.motorLight.setBackgroundResource(R.drawable.light_red)
        }
    }

    private fun cameraConnected(status: Boolean){
        binding.settingBtn.isEnabled = status
        binding.galleryBtn.isEnabled = status
        binding.cameraBtn.isEnabled = status
        binding.flashBtn.isEnabled = status
        if(status){
            binding.cameraLight.setBackgroundResource(R.drawable.light_green)
        }
        else{
            binding.cameraLight.setBackgroundResource(R.drawable.light_red)
        }
    }

    private fun socketStart(status: Boolean){
        binding.cameraLy.isEnabled = status
        binding.motorLy.isEnabled = status
        if(status){
            binding.cameraLight.setBackgroundResource(R.drawable.light_red)
            binding.motorLight.setBackgroundResource(R.drawable.light_red)
        }
        else{
            binding.cameraLight.setBackgroundResource(R.drawable.light_gray)
            binding.motorLight.setBackgroundResource(R.drawable.light_gray)
        }
    }

    override fun onDestroy() {
        intent = Intent(this, ForceGroundService::class.java)
        intent.action = ForceGroundService.ACTION_STOP_FOREGROUND_SERVICE
        startForegroundService(intent)
        super.onDestroy()
    }

    override fun onJoyStickMoved(id: Int, r: Int, theta: Int) {
        Log.e("XDD", "Range: $r, Float: $theta")

        if((motorClient != null && motorDelay == 0L) || r == 0){
            Thread{
                motorDelay = System.currentTimeMillis()
                motorClient!!.sendMessage("MOTOR$$r$$theta$")
            }.start()
        }
    }
}