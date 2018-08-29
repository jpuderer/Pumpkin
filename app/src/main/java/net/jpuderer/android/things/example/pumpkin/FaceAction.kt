package net.jpuderer.android.things.example.pumpkin

import android.support.annotation.DrawableRes

enum class FaceAction(val frames: List<ActionFrame>,
                      val soundId: Int?,
                      val soundDrivenAnimation: Boolean,
                      val loop: Boolean) {
    IDLE(listOf(
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_1, 3000L),
            ActionFrame(R.drawable.eyes_default_01, R.drawable.mouth_default_1,100L),
            ActionFrame(R.drawable.eyes_default_02, R.drawable.mouth_default_1,1000L),
            ActionFrame(R.drawable.eyes_default_03, R.drawable.mouth_default_1,2000L),
            ActionFrame(R.drawable.eyes_default_04, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_05, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_06, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_05, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_04, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_03, R.drawable.mouth_default_1,2000L),
            ActionFrame(R.drawable.eyes_default_07, R.drawable.mouth_default_1,100L),
            ActionFrame(R.drawable.eyes_default_08, R.drawable.mouth_default_1,2000L),
            ActionFrame(R.drawable.eyes_default_09, R.drawable.mouth_default_2,10L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_3,10L),
            ActionFrame(R.drawable.eyes_default_11, R.drawable.mouth_default_4,10L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_5,10L),
            ActionFrame(R.drawable.eyes_default_09, R.drawable.mouth_default_6,10L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_11, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_09, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_08, R.drawable.mouth_default_7,3000L),
            ActionFrame(R.drawable.eyes_default_12, R.drawable.mouth_default_7,2000L),
            ActionFrame(R.drawable.eyes_default_13, R.drawable.mouth_default_7,1000L),
            ActionFrame(R.drawable.eyes_default_14, R.drawable.mouth_default_7,1000L),
            ActionFrame(R.drawable.eyes_default_15, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_16, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_17, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_16, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_15, R.drawable.mouth_default_7,20L),
            ActionFrame(R.drawable.eyes_default_18, R.drawable.mouth_default_7,1000L),
            ActionFrame(R.drawable.eyes_default_19, R.drawable.mouth_default_7,500L),
            ActionFrame(R.drawable.eyes_default_08, R.drawable.mouth_default_7,500L),
            ActionFrame(R.drawable.eyes_default_09, R.drawable.mouth_default_6,10L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_5,10L),
            ActionFrame(R.drawable.eyes_default_11, R.drawable.mouth_default_4,10L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_3,10L),
            ActionFrame(R.drawable.eyes_default_09, R.drawable.mouth_default_2,10L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_11, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_10, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_09, R.drawable.mouth_default_1,20L)
    ), null, false, true),
    SAD(listOf(
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_1,1200L),
            ActionFrame(R.drawable.eyes_sad_01, R.drawable.mouth_default_1,400L),
            ActionFrame(R.drawable.eyes_sad_02, R.drawable.mouth_default_1,1000L),
            ActionFrame(R.drawable.eyes_sad_03, R.drawable.mouth_default_1,1000L),
            ActionFrame(R.drawable.eyes_sad_04, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_05, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_06, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_07, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_08, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_09, R.drawable.mouth_default_1,1800L),
            ActionFrame(R.drawable.eyes_sad_08, R.drawable.mouth_default_1,80L),
            ActionFrame(R.drawable.eyes_sad_07, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_06, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_05, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_04, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_03, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_02, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_sad_01, R.drawable.mouth_default_1,20L)
    ), null, false, false),
    HEARTS(listOf(
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_1,1000L),
            ActionFrame(R.drawable.eyes_hearts_01, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_02, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_03, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_04, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_05, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_06, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_07, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_08, R.drawable.mouth_default_1,500L),
            ActionFrame(R.drawable.eyes_hearts_09, R.drawable.mouth_default_1,200L),
            ActionFrame(R.drawable.eyes_hearts_10, R.drawable.mouth_default_1,500L),
            ActionFrame(R.drawable.eyes_hearts_09, R.drawable.mouth_default_1,200L),
            ActionFrame(R.drawable.eyes_hearts_08, R.drawable.mouth_default_1,500L),
            ActionFrame(R.drawable.eyes_hearts_07, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_06, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_05, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_04, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_03, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_02, R.drawable.mouth_default_1,40L),
            ActionFrame(R.drawable.eyes_hearts_01, R.drawable.mouth_default_1,40L)
    ), null, false, false),
    JOKE(listOf(
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_1,500L),
            ActionFrame(R.drawable.eyes_joke_01, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_joke_02, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_joke_03, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_joke_04, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_joke_05, R.drawable.mouth_default_1,500L),
            ActionFrame(R.drawable.eyes_joke_09, R.drawable.mouth_default_1,200L),
            ActionFrame(R.drawable.eyes_joke_05, R.drawable.mouth_default_1,500L),
            ActionFrame(R.drawable.eyes_joke_09, R.drawable.mouth_default_1,200L),
            ActionFrame(R.drawable.eyes_joke_05, R.drawable.mouth_default_1,500L),
            ActionFrame(R.drawable.eyes_joke_06, R.drawable.mouth_default_1,800L),
            ActionFrame(R.drawable.eyes_joke_07, R.drawable.mouth_default_1,800L),
            ActionFrame(R.drawable.eyes_joke_09, R.drawable.mouth_default_1,300L),
            ActionFrame(R.drawable.eyes_joke_07, R.drawable.mouth_default_1,800L),
            ActionFrame(R.drawable.eyes_joke_09, R.drawable.mouth_default_1,300L),
            ActionFrame(R.drawable.eyes_joke_07, R.drawable.mouth_default_1,800L),
            ActionFrame(R.drawable.eyes_joke_08, R.drawable.mouth_default_1,800L),
            ActionFrame(R.drawable.eyes_joke_07, R.drawable.mouth_default_1,800L),
            ActionFrame(R.drawable.eyes_joke_08, R.drawable.mouth_default_1,800L),
            ActionFrame(R.drawable.eyes_joke_07, R.drawable.mouth_default_1,800L)
    ), null, false, false),
    SPEAKING(listOf(
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_2,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_3,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_4,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_5,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_6,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_7,20L)
    ), null, true, false),
    SPOOKING(listOf(
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_1,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_2,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_3,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_4,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_5,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_6,20L),
            ActionFrame(R.drawable.eyes_default_00, R.drawable.mouth_default_7,20L)
    ), null, true, false)
}

data class ActionFrame(@DrawableRes val drawableEyesId: Int,
                       @DrawableRes val drawableMouthId: Int,
                       val durationMillis: Long)