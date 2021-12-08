package com.xdd.twitch_car

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.jar.Attributes

class JoyStick: SurfaceView, SurfaceHolder.Callback{
    constructor(context: Context): super(context)

    constructor(context: Context, attributes: AttributeSet): super(context, attributes){
        holder.addCallback(this)
    }

    constructor(context: Context, attributes: AttributeSet, style: Int): super(context, attributes, style)

    override fun surfaceCreated(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    private fun drawJoyStick(newX: Float, newY: Float){
        if(holder.surface.isValid){
            val colors = Paint().apply {
                setARGB(255, 50, 50 ,50)
            }
            val canvas = holder.lockCanvas().apply {
                drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                drawCircle()
            }
        }
    }

}