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
import android.hardware.SensorManager
import com.leinardi.android.things.driver.hcsr04.Hcsr04SensorDriver
import java.io.IOException


class MainActivity : Activity(), TextToSpeech.OnInitListener, SensorEventListener, SoundLevelListener {
    private lateinit var mFace : Face
    private lateinit var mTts : TextToSpeech
    private lateinit var mSoundLevel: SoundLevel
    private lateinit var mSoundPool : SoundPool
    private lateinit var mSoundPoolIds : List<Int>
    private lateinit var mProximitySensorDriver: Hcsr04SensorDriver
    private lateinit var mSensorManager: SensorManager
    private lateinit var mGreetings : Array<String>
    private val mRandom = Random()

    // True whenever there is no sound output
    private var silent : Boolean = true

    // Ultrasonic ranging distance (in cm)
    private var distances = LinkedList(listOf(400f, 400f, 400f, 400f, 400f))

    // Proxmity according to ultrasonic sensor: CLOSE, MEDIUM, FAR
    private var proximity : Int = PROXIMITY_FAR

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
        private val TAG = "Pumpkin"

        private const val DEBUG = false
        private const val LED_INTENSITY = 1

        private const val VOICE_NAME = "en-us-x-sfg#male_1-local"

        // Schedule a random event according to interval
        private const val RANDOM_EVENT_INTERVAL : Long = 30000

        private const val EVENTLOOP_MSG_START = 0
        private const val EVENTLOOP_MSG_RANDOM = 1
        private const val EVENTLOOP_MSG_PROXIMITY = 2
        private const val EVENTLOOP_MSG_NOISY = 3
        private const val EVENTLOOP_MSG_SILENCE = 4
        private const val EVENTLOOP_MSG_VOICE_REC = 5

        private const val PROXIMITY_FAR = 0
        private const val PROXIMITY_CLOSE = 2
    }

    // Handy utility function for dumping byte arrays
    fun ByteArray.toHex() = this.joinToString(separator = "") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGreetings = resources.getStringArray(R.array.greetings);

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

        mSoundLevel = SoundLevel(this, this)
        mSoundLevel.start()

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

        // Start event loop
        startEventLoop()
    }

    // Sound level changed
    var lastSound = 0L;
    override fun onSoundLevel(level : Int) {
        mFace.soundLevel = level

        if (level != 0) {
            lastSound = SystemClock.uptimeMillis()
            if (silent) {
                silent = false
                handler?.sendEmptyMessage(EVENTLOOP_MSG_NOISY)
            }
        } else if (!silent) {
            val delta = SystemClock.uptimeMillis() - lastSound;
            // Report silence when we've seen no wave for data for a second.
            if (delta > 1000) {
                silent = true
                handler?.sendEmptyMessage(EVENTLOOP_MSG_SILENCE)
            }
        }
    }

    // Ultrasonic accuracy changed
    override fun onAccuracyChanged(sensor: Sensor?, value: Int) {
        Log.i(TAG, "Accuracy changed: ${sensor}, ${value}")
    }

    // Ultrasonic distance changed
    override fun onSensorChanged(event: SensorEvent?) {
        val value = event?.values?.get(0) ?: 0f
        if (DEBUG) Log.i(TAG, "Sensor changed: ${value}")

        if (value <= 0)
            return

        // Calculate moving average
        distances.push(value)
        distances.removeLast()
        val distance = distances.average()
        if (DEBUG) Log.i(TAG, "Moving average: ${distance}")


        // There are gaps in the range values for hysteresis
        val newProximity = when (distance) {
            in 0..100 -> PROXIMITY_CLOSE
            else -> PROXIMITY_FAR
        }

        // Update the proximity
        if (proximity != newProximity) {
            val message = Message.obtain(handler, EVENTLOOP_MSG_PROXIMITY, proximity, newProximity)
            handler?.sendMessage(message)
            proximity = newProximity
        }
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

        mSoundLevel.stop()

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

                    when (msg.what) {
                        EVENTLOOP_MSG_START -> {
                            // Do Nothing
                        }
                        EVENTLOOP_MSG_NOISY -> {
                            Log.d(TAG, "Things got noisy.")
                        }
                        EVENTLOOP_MSG_SILENCE -> {
                            Log.d(TAG, "Sound stopped. Back to idle animation.")
                            mFace.setAction(FaceAction.IDLE)
                        }
                    }

                    // Schedule a random event for RANDOM_EVENT_INTERVAL after this event
                    // Remove any current random events, since we don't want them to happen to
                    // close together
                    removeMessages(EVENTLOOP_MSG_RANDOM)
                    sendEmptyMessageDelayed(EVENTLOOP_MSG_RANDOM, RANDOM_EVENT_INTERVAL)

                    // Don't process the event at all if there is sound still being produced
                    if (!silent) {
                        Log.d(TAG, "Ignoring event.  Sound event in progress.")
                        return
                    }

                    when (msg.what) {
                        EVENTLOOP_MSG_START,
                        EVENTLOOP_MSG_NOISY,
                        EVENTLOOP_MSG_SILENCE -> {
                            // Already processed
                        }
                        EVENTLOOP_MSG_RANDOM -> {
                            Log.d(TAG, "Random event!")
                            // Just say something if someone is close
                            if (proximity == PROXIMITY_CLOSE) {
                                saySomething()
                            } else {
                                val set = setOf(
                                        FaceAction.HEARTS,
                                        FaceAction.SPOOKING)
                                val action = set.shuffled().take(1)[0]
                                when (action) {
                                    FaceAction.SPOOKING -> { soundEffect() }
                                }
                                mFace.setAction(action)
                            }
                        }
                        EVENTLOOP_MSG_PROXIMITY -> {
                            Log.d(TAG, "Proximity: ${msg.arg1} --> ${msg.arg2}")

                            // Don't do anything if proximity is decreasing
                            if (msg.arg2 <= msg.arg1) return;

                            when (proximity) {
                                PROXIMITY_CLOSE -> saySomething()
                            }
                        }
                        EVENTLOOP_MSG_VOICE_REC -> {
                            Log.d(TAG, "Voice recognition")
                        }
                        else -> {
                            Log.d(TAG, "Unknown event: ${msg.what}")
                        }
                    }
                }
            }
        }

        // Start the event loop
        handler?.sendEmptyMessage(EVENTLOOP_MSG_START)
    }

    fun stopEventLoop() {
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

    fun saySomething(view : View? = null) {
        // Select a random hello.
        val helloLength = mGreetings.size
        val greatingId = mRandom.nextInt(helloLength)
        val hello = mGreetings[greatingId]

        Log.d(TAG, "Playing greating id: " + greatingId)

        mFace.setAction(FaceAction.SPEAKING)
        mTts.speak(hello,
                TextToSpeech.QUEUE_FLUSH, // Drop all pending entries in the playback queue.
                null, "hello")

        // Needed for fake visualizer
        mSoundLevel.startSound()
    }

    fun soundEffect(view: View? = null) {
        mFace.setAction(FaceAction.SPOOKING)
        val soundId = mRandom.nextInt(mSoundPoolIds.size + 1)
        Log.d(TAG, "Playing sound id: " + soundId)
        mSoundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        // Needed for fake visualizer
        mSoundLevel.startSound()
    }
}
