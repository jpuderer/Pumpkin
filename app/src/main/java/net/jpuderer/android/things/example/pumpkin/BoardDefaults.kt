/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jpuderer.android.things.example.pumpkin

import android.os.Build

object BoardDefaults {
    val DEVICE_RPI3 = "rpi3"
    val DEVICE_IMX7D_PICO = "imx7d_pico"

    val spiGpioForLedControl : String
        get() {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "SPI0.0"
                DEVICE_IMX7D_PICO -> return "SPI3.1"
                else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
            }
        }

    val hc04TriggerPin: String
        get() {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "BCM21"
                DEVICE_IMX7D_PICO -> return "GPIO6_IO14"
                else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
            }
        }

    val hc04EchoPin: String
        get() {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "BCM20"
                DEVICE_IMX7D_PICO -> return "GPIO6_IO15"
                else -> throw IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE)
            }
        }
}
