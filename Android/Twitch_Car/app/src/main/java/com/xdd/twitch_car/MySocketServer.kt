package com.xdd.twitch_car

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.ServerSocket
import java.io.PrintStream
import java.lang.StringBuilder
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.Base64.getEncoder
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MySocketServer{

    private var context: Context

    constructor(context: Context){
        this.context = context.applicationContext
    }

    constructor(context: Context, serverPort: Int){
        this.context = context.applicationContext

        mySocketServer = ServerSocket(serverPort)
    }

    fun deviceAcceptListener(client: (MyClient) -> Unit){
        Thread {
            while (true){
                if(mySocketServer != null){
                    try{
                        val socket = mySocketServer!!.accept()
                        socket.setPerformancePreferences(1, 2, 0)
                        socket.tcpNoDelay = true;

                        val myClient = MyClient(socket){
                            client(it)
                        }

                        clientList.add(myClient)
                        Log.d("Client Connected", "Client connected : ${socket.inetAddress.hostAddress}")
                    }
                    catch (e: Exception){
                        Log.e("FUCK", e.message.toString())
                    }
                }
                else{
                    Thread.sleep(500)
                }
            }
        }.start()
    }

    companion object{
        @JvmStatic
        private var mySocketServer: ServerSocket? = null

        private val clientList = ArrayList<MyClient>()

        fun stopSocketServer(){
            clientList.forEach {
                it.socket?.close()
                it.socket = null
            }
            mySocketServer?.close()
            mySocketServer = null
        }

    }
}

class MyClient(var socket: Socket?, identity: (MyClient) -> Unit){

    var who: String? = null
    private val `in` = DataInputStream(socket?.getInputStream())
    //private val `in` = BufferedReader(InputStreamReader(socket?.getInputStream()))
    private var command = ""

    init {
        Timer().schedule(100, 1000){
            if(who != null){
                Log.e("WHO", who!!)
                identity(this@MyClient)
                this.cancel()
            }

            sendMessage("WHO ?")
        }
        Thread{
            while (who == null){
                var buffer = ""
                do {
                    buffer += byteArrayOf(`in`.readByte()).decodeToString()
                } while (buffer[buffer.length - 1] != '\n')
                Log.e("bytes", buffer)

                buffer.split("$").let {
                    if(it[0] == "WHO"){
                        who = it[1]
                    }
                }
            }
        }.start()
    }

    fun getIpAddress(): String?{
        if (socket != null) {
            return socket!!.inetAddress.hostAddress
        }
        return null
    }

    fun onMessageInputListener(message: (String) -> Unit, image: (Bitmap?) -> Unit){
        Thread{
            var buffer = ""

            while (true) when{
                socket == null -> {
                    break
                }
                who == null -> {
                    break
                }
                else -> {
                    try{
                        do {
                            buffer += byteArrayOf(`in`.readByte()).decodeToString()
                        } while (buffer[buffer.length - 1] != '\n')
                        Log.e("bytes", buffer)
                        message(buffer)

                        buffer.split("$").let {
                            if(it.isNotEmpty()){
                                when(it[0]){
                                    "IMAGE" -> {
                                        Log.e("IMAGE", "in")
                                        val imageBytes = ByteArray(it[1].toInt())
                                        var imageLength = 0
                                        while (imageLength < imageBytes.size){
                                            imageLength += `in`.read(imageBytes, imageLength, imageBytes.size - imageLength)
                                            val chunkCheck = imageBytes.toString(Charset.forName("UTF-8"))
                                            val imageDoneIndex = chunkCheck.indexOf("IMAGE DONE\n")
                                            if(imageDoneIndex != -1){
                                                imageLength = imageDoneIndex
                                                buffer = chunkCheck.substring(imageDoneIndex + 11)
                                                break
                                            }
                                        }

                                        Log.e("IMAGE", imageBytes.toBase64())

                                        Log.e("IMAGE", "out")

                                        if(imageLength == imageBytes.size) {
                                            buffer = ""
                                        }
                                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageLength)
                                        if(bitmap != null){
                                            image(bitmap)
                                        }
                                    }
                                    else -> buffer = ""
                                }
                            }
                        }
                    } catch (e: Exception){
                        Log.e("ERROR", e.message.toString())
                    }
                }
            }
        }.start()
    }

    fun startCamera(){
        if(who == "CAMERA"){
            sendMessage("CAMERA\$START$")
        }
    }

    fun stopCamera(){
        if(who == "CAMERA"){
            sendMessage("CAMERA\$STOP$")
        }
    }

    fun startFlashLight(){
        if(who == "CAMERA"){
            sendMessage("FLASHLIGHT\$START$")
        }
    }

    fun stopFlashLight(){
        if(who == "CAMERA"){
            sendMessage("FLASHLIGHT\$STOP$")
        }
    }

    fun sendMessage(output: String): Boolean {
        if (socket != null) {
            val out = PrintStream(socket!!.getOutputStream())
            out.print(output)

            return true
        }
        return false
    }

    fun ByteArray.toBase64(): String =
        String(Base64.encode(this, Base64.DEFAULT))
}