package com.xdd.twitch_car

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.core.content.ContextCompat
import java.util.jar.Attributes
import kotlin.math.*
import kotlin.properties.Delegates

class JoyStick: SurfaceView, SurfaceHolder.Callback, View.OnTouchListener{

    private var centerX by Delegates.notNull<Float>()
    private var centerY by Delegates.notNull<Float>()
    private var baseRadius by Delegates.notNull<Float>()
    private var hatRadius by Delegates.notNull<Float>()
    private var joyStickCallback: JoyStickListener

    constructor(context: Context): super(context){
        holder.addCallback(this)
        setOnTouchListener(this)
        joyStickCallback = context as JoyStickListener
    }

    constructor(context: Context, attributes: AttributeSet): super(context, attributes){
        holder.addCallback(this)
        setOnTouchListener(this)
        joyStickCallback = context as JoyStickListener
    }

    constructor(context: Context, attributes: AttributeSet, style: Int): super(context, attributes, style){
        holder.addCallback(this)
        setOnTouchListener(this)
        joyStickCallback = context as JoyStickListener
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        setupDimensions()
        drawJoyStick(centerX, centerY, false)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    private fun setupDimensions(){
        centerX = width / 2f
        centerY = width / 2f
        baseRadius = width.coerceAtMost(height) / 3f
        hatRadius = width.coerceAtMost(height) / 8f
    }

    private fun drawJoyStick(newX: Float, newY: Float, pressed: Boolean){
        if(holder.surface.isValid){
            val alpha = if(pressed) 225 else 200
            val colors = Paint()
            val canvas = holder.lockCanvas().apply {
                drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                colors.setARGB(alpha, 50, 50 ,50)
                drawCircle(centerX, centerY, baseRadius, colors)
                colors.setARGB(alpha, 125, 125, 125)
                drawCircle(newX, newY, hatRadius, colors)
            }
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (v == this && isEnabled) {
            val displacement = (sqrt((event.x - centerX).toDouble().pow(2.0) + (event.y - centerY).toDouble().pow(2.0))).toFloat() / 1.5f
            if(event.action != MotionEvent.ACTION_UP){
                if(displacement < baseRadius){
                    val constrainedX = centerX - (centerX - event.x) / 1.5f
                    val constrainedY = centerY - (centerY - event.y) / 1.5f
                    drawJoyStick(constrainedX, constrainedY, true)
                    calculateRTheta(centerY - constrainedY, centerX - constrainedX)
                }
                else{
                    val ratio = baseRadius / displacement / 1.5f
                    val constrainedX = (centerX + (event.x - centerX) * ratio)
                    val constrainedY = (centerY + (event.y - centerY) * ratio)
                    drawJoyStick(constrainedX, constrainedY, true)
                    calculateRTheta(centerY - constrainedY, centerX - constrainedX)
                }
            }
            else{
                drawJoyStick(centerX, centerY, false)
                joyStickCallback.onJoyStickMoved(id,0, 0)
            }
        }

        return true
    }

    private fun calculateRTheta(x: Float, y: Float){
        val r = sqrt(x * x + y * y)
        val cosTheta = (r * r + x * x - y * y) / (2 * r * x)
        val theta = (acos(cosTheta) * 180 / Math.PI.toFloat()).toInt()

        if(y > 0){
            joyStickCallback.onJoyStickMoved(id, (r / baseRadius * 1023).toInt(), theta)
        }
        else{
            joyStickCallback.onJoyStickMoved(id, (r / baseRadius * 1023).toInt(), 360 - theta)
        }
    }

    interface JoyStickListener{
        fun onJoyStickMoved(id: Int, r: Int, theta: Int)
    }

}