package net.jpuderer.android.things.example.pumpkin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.io.IOException

import com.nilhcem.androidthings.driver.max72xx.LedControl;
import kotlin.math.max
import kotlin.math.min

class Face constructor(private val context: Context, private val intensity : Int) {

    companion object {
        private val TAG = Face::class.java.simpleName!!
        private val HANDLER_MSG_SHOW = 1
        private val HANDLER_MSG_STOP = 2
    }

    private lateinit var ledControl: LedControlLocal

    private var curFrameIdx = 0
    private var action = FaceAction.IDLE

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    // Public variable.  Gets set to animate face for sound driven animations
    var soundLevel : Int = 0

    private fun initLedControl(clearDisplay : Boolean = true) {
        for (i in 0..ledControl.deviceCount - 1) {
            ledControl.setIntensity(i, intensity)
            ledControl.shutdown(i, false)
            if (clearDisplay) ledControl.clearDisplay(i)
        }
    }

    fun start() {
        ledControl = LedControlLocal(BoardDefaults.spiGpioForLedControl, 8)
        initLedControl()

        handlerThread = HandlerThread("FrameThread").apply {
            start()

            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    if (msg.what != HANDLER_MSG_SHOW) {
                        return
                    }

                    try {
                        val frame = action.frames[curFrameIdx]
                        val eyesBmp = BitmapFactory.decodeResource(context.resources, frame.drawableEyesId)
                        val mouthBmp = BitmapFactory.decodeResource(context.resources, frame.drawableMouthId)

                        drawEyes(eyesBmp)
                        drawMouth(mouthBmp)

                        if (action.soundDrivenAnimation) {
                            if (soundLevel > curFrameIdx) {
                                // increase current frame by one
                                curFrameIdx = min(curFrameIdx + 1, action.frames.size - 1)
                            } else if (soundLevel < curFrameIdx) {
                                // decrease current frame by one
                                curFrameIdx = max(curFrameIdx - 1, 0)
                            }
                        } else {
                            curFrameIdx = (curFrameIdx + 1) % action.frames.size

                            if (curFrameIdx == 0 && !action.loop) {
                                action = FaceAction.IDLE
                            }
                        }
                        sendEmptyMessageDelayed(HANDLER_MSG_SHOW, frame.durationMillis)
                    } catch (e: IOException) {
                        Log.e(TAG, "Error displaying frame", e)
                    }
                }
            }
        }
        handler?.sendEmptyMessage(HANDLER_MSG_SHOW)
    }

    private fun drawEyes(bitmap: Bitmap) {
        val scaled = Bitmap.createScaledBitmap(bitmap, 8 * 4, 8, true)
        for (row in 0..7) {
            for (cell in 0..3) {
                var value = 0
                for (col in 0..7) {
                    value = value or if (scaled.getPixel(cell * 8 + col, row) == Color.WHITE) 0x80 shr col else 0
                }
                ledControl.setRow(4 - cell - 1, row, value.toByte())
            }
        }
    }

    // Display is chained to eyes, and flipped 180 degrees
    private fun drawMouth(bitmap: Bitmap) {
        val scaled = Bitmap.createScaledBitmap(bitmap, 8 * 4, 8, true)
        for (row in 0..7) {
            for (cell in 0..3) {
                var value = 0
                for (col in 0..7) {
                    value = value or if (scaled.getPixel((3 - cell) * 8 + (7 - col), 7 - row) ==
                            Color.WHITE) 0x80 shr col else 0
                }
                ledControl.setRow(8 - cell - 1, row, value.toByte())
            }
        }
    }

    fun setAction(action: FaceAction) {
        // Reset the LED control, to work around any corruption issues caused by EMI
        ledControl.reset(false)
        initLedControl(false)

        handler?.removeMessages(HANDLER_MSG_SHOW)
        this@Face.action = action
        curFrameIdx = 0
        handler?.sendEmptyMessage(HANDLER_MSG_SHOW)
    }

    fun stop() {
        handler?.sendEmptyMessage(HANDLER_MSG_STOP)

        try {
            handlerThread?.quitSafely()
            ledControl.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing LED matrix", e)
        } finally {
            handler = null
            handlerThread = null
        }
    }
}
