package net.jpuderer.android.things.example.pumpkin

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import java.util.*
import android.media.AudioManager
import android.media.SoundPool
import android.media.AudioAttributes
import android.media.audiofx.Visualizer
import kotlin.math.absoluteValue
import android.hardware.SensorManager
import com.leinardi.android.things.driver.hcsr04.Hcsr04SensorDriver
import java.io.IOException

// TODO:
// -> Animate sounds

class MainActivity : Activity(), TextToSpeech.OnInitListener, SensorEventListener {
    private lateinit var mFace : Face
    private lateinit var mTts : TextToSpeech
    private lateinit var mSoundPool : SoundPool
    private lateinit var mSoundPoolIds : List<Int>
    private lateinit var mVisualizer : Visualizer
    private lateinit var mProximitySensorDriver: Hcsr04SensorDriver
    private lateinit var mSensorManager: SensorManager
    private val mRandom = Random()

    // True whenever there is no sound output
    private var silent : Boolean = true

    // Handler for event loop.  We basically react to stimulus and post things to
    // the loop, and post random events with a delay to the loop, and that pretty
    // much controls what the pumkin does
    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    // Register listener for ultrasonic ranger (HC-04) updates
    private val mDynamicSensorCallback = object : SensorManager.DynamicSensorCallback() {
        override fun onDynamicSensorConnected(sensor: Sensor) {
            if (sensor.type == Sensor.TYPE_PROXIMITY) {
                mSensorManager.registerListener(this@MainActivity,
                        sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    companion object {
        private val TAG = Face::class.java.simpleName!!

        private val LED_INTENSITY = 1

        private val VOICE_NAME = "en-us-x-sfg#male_1-local"

        private val EVENTLOOP_MSG_STOP = 1
        private val EVENTLOOP_MSG_RANDOM = 2
        private val EVENTLOOP_MSG_PROXIMITY = 3
        private val EVENTLOOP_MSG_NOISY = 4
        private val EVENTLOOP_MSG_SILENCE = 5
        private val EVENTLOOP_MSG_VOICE_REC = 6
    }

    val GREATINGS : List<String> = listOf(
            "Hello",
            "Happy Halloween!",
            "I'm a pumpkin",
            "So? Anything interesting going on?",
            "Spookey.  It's a talking pumkin",
            "Hey",
            "So, what's happening?"
    )

    // Handy utility function for dumping byte arrays
    fun ByteArray.toHex() = this.joinToString(separator = "") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start the face
        mFace = Face(this, LED_INTENSITY)
        mFace.start()

        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = TextToSpeech(this,
                this  // TextToSpeech.OnInitListener
        )

        // Set the volume to maximum
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)

        // Create a sound pool to play SPOOOOKEY sounds
        mSoundPool = SoundPool.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setMaxStreams(1)
                .build()

        // Load all raw resources into the sound pool
        mSoundPoolIds = R.raw::class.java.fields.map { field ->
            mSoundPool.load(this, field.getInt(null), 1)
        }

        // Setup audio visualizer callback.  We use this to both animate the faces, and to
        // figure out when there is active audio output.
        Log.d(TAG, "Creating visualizer...")
        mVisualizer = Visualizer(0)
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0])
        mVisualizer.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    var silenceCounter : Int = 0

                    override fun onWaveFormDataCapture(visualizer: Visualizer,
                                                       bytes: ByteArray, samplingRate: Int) {
                        val average = bytes.fold(0) { sum, b ->
                            sum + ((b.toInt() and 0xff) - 0x80).absoluteValue} / bytes.size
                        Log.d(TAG, "Average: ${average}, Level: ${average / 8}")
                        val rms = Visualizer.MeasurementPeakRms()
                        mVisualizer.getMeasurementPeakRms(rms)
                        Log.d(TAG, "Peak: ${rms.mPeak}, RMS: ${rms.mRms}}")
                        val level = ((9600 + rms.mRms) / 1200).absoluteValue
                        Log.d(TAG, "RMS level: ${level}")
                        mFace.soundLevel = level

                        // Report silence when we've seen no wave for data for a second.
                        if (silent)  {
                            if (level != 0) {
                                silent = false
                                silenceCounter = 1000 / Visualizer.getMaxCaptureRate()
                            }
                        } else {
                            if (silenceCounter <= 0) {
                                silent = true
                                this@MainActivity.onSoundStopped()
                            } else {
                                silenceCounter -= 1
                            }
                        }
                    }

                    override fun onFftDataCapture(visualizer: Visualizer,
                                                  bytes: ByteArray, samplingRate: Int) {
                        Log.d(TAG, "Got FFT data!")
                    }
                }, Visualizer.getMaxCaptureRate(), true, false)
        mVisualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);
        var status = mVisualizer.setEnabled(true)
        Log.d(TAG, "mVisualizer.setEnabled: $status")

        // Start ultrasonic ranging
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager.registerDynamicSensorCallback(mDynamicSensorCallback)

        try {
            mProximitySensorDriver = Hcsr04SensorDriver(
                    BoardDefaults.hc04TriggerPin, BoardDefaults.hc04EchoPin)
            mProximitySensorDriver.registerProximitySensor()
        } catch (e: IOException) {
            Log.e(TAG, "Unable to initialize HC-04 ultrasonic sensor")
        }
    }

    // Ultrasonic accuracy changed
    override fun onAccuracyChanged(sensor: Sensor?, value: Int) {
        Log.i(TAG, "Accuracy changed: ${sensor}, ${value}");
    }

    // Ultrasonic distance changed
    override fun onSensorChanged(event: SensorEvent?) {
        Log.i(TAG, "Sensor changed: ${event?.values?.get(0)}")
    }

    fun onSoundStopped() {
        Log.d(TAG, "Sound stopped. Back to idle animation.")
        mFace.setAction(FaceAction.IDLE)
    }

    override fun onDestroy() {
        super.onDestroy()
        mFace.stop();

        mSensorManager.unregisterDynamicSensorCallback(mDynamicSensorCallback)
        mSensorManager.unregisterListener(this)
        mProximitySensorDriver.unregisterProximitySensor()
        try {
            mProximitySensorDriver.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing HC-04 ultrasonic sensor", e)
        }
        stopEventLoop()
    }

    // TextToSpeach onInit
    override fun onInit(status: Int) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            val result = mTts.setLanguage(Locale.US)

            // Try this someday for some interesting results.
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.")
            } else {
                // The TTS engine has been successfully initialized.

                // Set the voice
                for(voice in mTts.voices) {
                    if (voice.name.equals(VOICE_NAME))
                        mTts.setVoice(voice)
                }
                mTts.setPitch(0.4f)
                mTts.setSpeechRate(0.1f)
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.")
        }
    }

    fun startEventLoop() {
        handlerThread = HandlerThread("EventLoopThread").apply {
            start()

            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)

                    // TODO: Use a what statement to do something here!!!

                    // TODO: Post something random to the handler if there's nothing else scheduled
                }
            }
        }

        // TODO: Start with a random event.
        //handler?.sendEmptyMessage(HANDLER_MSG_SHOW)
    }

    fun stopEventLoop() {
        handler?.sendEmptyMessage(EVENTLOOP_MSG_STOP)

        handlerThread?.quitSafely()
        handler = null
        handlerThread = null
    }

    fun moodButtonClicked(view : View) {
        val action = when (view.id) {
            R.id.idleMoodButton -> FaceAction.IDLE
            R.id.loveMoodButton -> FaceAction.HEARTS
            R.id.sadMoodButton -> FaceAction.SAD
            else -> FaceAction.IDLE
        }
        mFace.setAction(action)
    }

    fun sayHello(view : View) {
        // Select a random hello.
        val helloLength = GREATINGS.size
        val hello = GREATINGS[mRandom.nextInt(helloLength)]

        mFace.setAction(FaceAction.SPEAKING)
        mTts.speak(hello,
                TextToSpeech.QUEUE_FLUSH, // Drop all pending entries in the playback queue.
                null, "hello")
    }

    fun soundEffect(view : View) {
        mFace.setAction(FaceAction.SPOOKING)
        mSoundPool.play(mRandom.nextInt(mSoundPoolIds.size),
                1.0f, 1.0f, 1, 0, 1.0f)
    }
}
