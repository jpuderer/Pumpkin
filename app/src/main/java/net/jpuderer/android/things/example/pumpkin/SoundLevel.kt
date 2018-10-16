package net.jpuderer.android.things.example.pumpkin

import android.content.Context
import android.graphics.BitmapFactory
import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.SystemClock
import android.util.Log
import java.io.IOException
import kotlin.math.absoluteValue

/**
 * Wraps the Android visualizer class to provide a simple level indication to
 * animate the face during sound events.
 *
 * It also provides "fake" sound levels for devices where the visualizer is broken
 * (such as the RPi3 unfortunately).
 */
class SoundLevel {
    companion object {
        private const val TAG = "SoundLevel"
        private const val DEBUG = false

        private val FAKEDATA_MSG_STOP = 0
        private val FAKEDATA_MSG_LEVEL = 1

        private val FAKEDATA_INTERVAL = 200L
        private val DEFAULT_FAKEDATA = intArrayOf(
                1, 2, 3, 3, 4,
                4, 5, 5, 5, 6,
                6, 7, 7, 6, 6,
                7, 7, 6, 6, 7,
                7, 6, 7, 6, 7
        )
    }

    private lateinit var mVisualizer: Visualizer
    private lateinit var mSoundLevelListener: SoundLevelListener
    private var mHandler: Handler? = null
    private var isFakeVisualizer : Boolean = false

    private val mDataCaptureListener = object : Visualizer.OnDataCaptureListener {
        override fun onWaveFormDataCapture(visualizer: Visualizer,
                                           bytes: ByteArray, samplingRate: Int) {
            val average = bytes.fold(0) { sum, b ->
                sum + ((b.toInt() and 0xff) - 0x80).absoluteValue
            } / bytes.size
            if (DEBUG) Log.d(TAG, "Average: ${average}, Level: ${average / 8}")
            val rms = Visualizer.MeasurementPeakRms()
            mVisualizer.getMeasurementPeakRms(rms)
            if (DEBUG) Log.d(TAG, "Peak: ${rms.mPeak}, RMS: ${rms.mRms}}")
            val level = ((9600 + rms.mRms) / 1200).absoluteValue
            if (DEBUG) Log.d(TAG, "Level: ${level}")
            mSoundLevelListener.onSoundLevel(level)
        }

        override fun onFftDataCapture(visualizer: Visualizer,
                                      bytes: ByteArray, samplingRate: Int) {
            if (DEBUG) Log.d(TAG, "Got FFT data!")
        }
    }

    private var mFakeDataThread = HandlerThread("FakeDataThread").apply {
        HandlerThread@this.start()
        mHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                if (msg.what == FAKEDATA_MSG_STOP) {
                    removeMessages(FAKEDATA_MSG_LEVEL)
                    return
                }

                // Send level information
                mSoundLevelListener.onSoundLevel(msg.arg1)
                if (DEBUG) Log.d(TAG, "Level: ${msg.arg1}")

                // If no other data, send 0 level data until new data arrives or stopped
                if (!hasMessages(FAKEDATA_MSG_LEVEL)) {
                    val message = obtainMessage(FAKEDATA_MSG_LEVEL)
                    sendEmptyMessageDelayed(FAKEDATA_MSG_LEVEL, FAKEDATA_INTERVAL)
                }
            }
        }
    }

    constructor(context : Context, listener : SoundLevelListener) {
        mSoundLevelListener = listener
    }

    fun start() {
        // Setup audio visualizer callback.  We use this to both animate the faces, and to
        // figure out when there is active audio output.
        Log.d(TAG, "Creating visualizer...")
        mVisualizer = Visualizer(0)
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0])
        mVisualizer.setDataCaptureListener(mDataCaptureListener
                , Visualizer.getMaxCaptureRate(), true, false)
        mVisualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);
        var status = mVisualizer.setEnabled(true)
        Log.d(TAG, "mVisualizer.setEnabled: $status")

        if (status != Visualizer.SUCCESS) {
            if (DEBUG) Log.d(TAG, "Running fake visualizer.")
            isFakeVisualizer = true
            startFakeVisualizer();
        }
    }

    fun stop() {
        if (isFakeVisualizer) {
            stopFakeVisualizer();
        } else {
            mVisualizer.release()
        }
    }

    /**
     * Inject fake sound level data for use when the real visualizer is not working.
     * Used default data if none is passed in.
     */
    fun startSound(data : IntArray = DEFAULT_FAKEDATA) {
        if (!isFakeVisualizer)
            return

        var delay = 0L
        for (level in data) {
            val message = mHandler?.obtainMessage(FAKEDATA_MSG_LEVEL, level, 0)
            mHandler?.sendMessageDelayed(message, delay)
            delay += FAKEDATA_INTERVAL
        }
    }

    private fun startFakeVisualizer() {
        mHandler?.sendEmptyMessage(FAKEDATA_MSG_LEVEL)
    }

    private fun stopFakeVisualizer() {
        mHandler?.sendEmptyMessage(FAKEDATA_MSG_STOP)
    }
}

interface SoundLevelListener {
    fun onSoundLevel(level : Int)
}