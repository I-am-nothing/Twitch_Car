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
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MySocketServer{

    private var context: Context

    constructor(context: Context){
        this.context = context.applicationContext
        if(mySocketServer == null){
            Toast.makeText(this.context, "You haven't initialize socket server yet", Toast.LENGTH_SHORT).show()
            throw Exception("You haven't initialize socket server yet!")
        }
    }

    constructor(context: Context, serverPort: Int){
        this.context = context.applicationContext

        mySocketServer = ServerSocket(serverPort)
        Toast.makeText(this.context, "Socket server starts listening devices", Toast.LENGTH_SHORT).show()
    }

    fun deviceAcceptListener(client: (MyClient) -> Unit){
        Thread {
            while (true){
                if(mySocketServer == null){
                    break
                }

                try{
                    val socket = mySocketServer!!.accept()
                    socket.setPerformancePreferences(1, 2, 0)
                    //socket.soTimeout = 1000
                    socket.tcpNoDelay = true;

                    val myClient = MyClient(socket)

                    clientList.add(myClient)
                    Log.d("Client Connected", "Client connected : ${socket.inetAddress.hostAddress}")

                    client(myClient)
                }
                catch (e: Exception){
                    Log.e("FUCK", e.message.toString())
                }
            }
        }.start()
    }

    companion object{
        @JvmStatic
        private var mySocketServer: ServerSocket? = null

        private val clientList = ArrayList<MyClient>()

        fun checkSocketServerStart(ok: () -> Unit){
            Timer().schedule(0, 500){
                if(mySocketServer != null){
                    ok()
                    this.cancel()
                }
            }
        }

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

class MyClient(var socket: Socket?){

    var who: String? = ""//null
    private val `in` = DataInputStream(socket?.getInputStream())
    //private val `in` = BufferedReader(InputStreamReader(socket?.getInputStream()))
    private var command = ""

    init {
        /*val ask = Timer().schedule(100, 1000){
            sendMessage("WHO ?")
        }
        Thread{
            val `in` = BufferedReader(InputStreamReader(socket?.getInputStream()))
            var inputLine: String
            while (`in`.readLine().also{ inputLine = it} != null) {
                Log.d("FUCK", "WHO:${inputLine}")
                val command = inputLine.split(" ")
                if(command.size != 2 && command[0] == "WHO"){
                    who = command[1]
                }
            }
            ask.cancel()
        }.start()*/
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
                    do {
                        buffer += byteArrayOf(`in`.readByte()).decodeToString()
                    } while (buffer[buffer.length - 1] != '\n')
                    Log.e("bytes", buffer)

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

                                    //Log.e("IMAGE", Base64.encodeToString(imageBytes, Base64.DEFAULT))
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

                    /*val index = inputString.indexOf("\n\r")

                    if(index == -1){

                    }
                    else{
                        val input = inputString.substring(0, index)

                        Log.d("INPUT", command)
                        when(command){
                            "IMAGE" -> {
                                val imageBytes = input.toByteArray(Charset.forName("UTF-8"))

                                Log.e("IMAGE_RECEIVE", imageBytes.size.toString())

                                image(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                            }
                            else -> {
                                when{
                                    input.contains("COMMAND") -> {

                                    }
                                    input.contains("IMAGE") -> {
                                        command = "IMAGE"
                                    }
                                    else -> {

                                    }
                                }
                            }
                        }
                        inputString.deleteRange(0, index)
                    }*/

                    /*var inputLine: String
                    while (`in`.readLine().also{ inputLine = it} != null) {
                        Log.d("COMMAND", inputLine)
                        Log.e("COMMAND", inputLine.length.toString())
                        /*val command = inputLine.split(" ")

                        if(command.isNotEmpty()){
                            when(command[0]){
                                "IMAGE" -> {
                                    if(command.size == 2){
                                        image(getImageStream(command[1].toInt()))
                                    }
                                }
                            }
                        }
                        message(inputLine)*/
                    }*/
                }
            }
        }.start()
    }

    fun startCamera(){
        if(who == "CAMERA"){
            sendMessage("CAMERA START")
        }
    }

    fun stopCamera(){
        if(who == "CAMERA"){
            sendMessage("CAMERA STOP")
        }
    }

    fun startFlashLight(){
        if(who == "CAMERA"){
            sendMessage("FLASHLIGHT START")
        }
    }

    fun stopFlashLight(){
        if(who == "CAMERA"){
            sendMessage("FLASHLIGHT STOP")
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
}