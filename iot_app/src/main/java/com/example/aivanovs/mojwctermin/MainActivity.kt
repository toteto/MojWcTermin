package com.example.aivanovs.mojwctermin

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.FirebaseDatabase
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    private val logger = LoggerFactory.getLogger(MainActivity::class.java)
    private val mHandler = Handler(Looper.getMainLooper())

    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mRestroomDoorClosed by lazy { mDatabase.getReference("restroomDoorClosed") }

    // IoT
    private val pms = PeripheralManagerService()
    private val mDoorSensorButton = Button(Ports.DOOR_PROXIMITY_BUTTON.value, Button.LogicState.PRESSED_WHEN_HIGH).apply {
        setDebounceDelay(TimeUnit.SECONDS.toMillis(1))
    }

    private val mDoorClosedIndicator = pms.openGpio(Ports.DOOR_CLOSED_INDICATOR.value).apply {
        setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        setActiveType(Gpio.ACTIVE_HIGH)
    }

    private val mDoorOpenIndicator = pms.openGpio(Ports.DOOR_OPENED_INDICATOR.value).apply {
        setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        setActiveType(Gpio.ACTIVE_HIGH)
    }

    private val mRegisteredGpioDevices = listOf(
            mDoorSensorButton, mDoorClosedIndicator, mDoorOpenIndicator)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showProgress(true)
        mDoorSensorButton.setOnButtonEventListener(doorSensorListener)
    }

    private val doorSensorListener = Button.OnButtonEventListener { _, doorClosed ->
        logger.debug("Door became ${if (doorClosed) "closed" else "open"}.")
        showProgress(false)
        mRestroomDoorClosed.setValue(doorClosed)
        updateStatusIndicator(doorClosed)
    }

    private fun updateStatusIndicator(doorClosed: Boolean) {
        mDoorClosedIndicator.value = doorClosed
        mDoorOpenIndicator.value = !doorClosed
    }

    override fun onDestroy() {
        mRegisteredGpioDevices.forEach { it.close() }
        super.onDestroy()
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            mHandler.postDelayed(progressRunnable, TimeUnit.MILLISECONDS.toMillis(100))
        } else {
            mHandler.removeCallbacks(progressRunnable)
        }
    }

    private val progressRunnable = Runnable {
        if (mDoorClosedIndicator.value) {
            mDoorClosedIndicator.value = false
            mDoorOpenIndicator.value = true
        } else {
            mDoorClosedIndicator.value = true
            mDoorOpenIndicator.value = false
        }
        showProgress(true)
    }
}
