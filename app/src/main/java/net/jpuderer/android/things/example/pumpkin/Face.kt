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

    private lateinit var mLedEyesControl: LedControlLocal
    private lateinit var mLedMouthControl: LedControlLocal


    private var curFrameIdx = 0
    private var action = FaceAction.IDLE

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    // Public variable.  Gets set to animate face for sound driven animations
    var soundLevel : Int = 0

    private fun initLedControl(clearDisplay : Boolean = true) {
        for (i in 0..mLedEyesControl.deviceCount - 1) {
            mLedEyesControl.setIntensity(i, intensity)
            mLedEyesControl.shutdown(i, false)
            if (clearDisplay) mLedEyesControl.clearDisplay(i)
        }
        for (i in 0..mLedMouthControl.deviceCount - 1) {
            mLedMouthControl.setIntensity(i, intensity)
            mLedMouthControl.shutdown(i, false)
            if (clearDisplay) mLedMouthControl.clearDisplay(i)
        }
    }

    fun start() {
        mLedEyesControl = LedControlLocal(BoardDefaults.spiGpioForLedEyesControl, 2)
        mLedMouthControl = LedControlLocal(BoardDefaults.spiGpioForLedMouthControl, 4)

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

                        mLedEyesControl.draw(eyesBmp)
                        mLedMouthControl.draw(mouthBmp)

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

    fun setAction(action: FaceAction) {
        // Reset the LED control, to work around any corruption issues caused by EMI
        mLedEyesControl.reset(false)
        mLedMouthControl.reset(false)
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
            mLedEyesControl.close()
            mLedMouthControl.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing LED matrix", e)
        } finally {
            handler = null
            handlerThread = null
        }
    }
}
