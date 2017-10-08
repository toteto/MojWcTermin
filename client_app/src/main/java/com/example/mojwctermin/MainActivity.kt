package com.example.mojwctermin

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity(), RestroomAvailabilityRepository.OnRestroomAvailabilityChange {
    private val mRestroomAvailabilityLabel by lazy { findViewById<TextView>(R.id.restroomStatusLabel) }
    private val mSubscribeButton by lazy { findViewById<Button>(R.id.btnSubscribe) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSubscribeButton.setOnClickListener {
            val started = RestroomAvailabilityService.started
            if (started)
                RestroomAvailabilityService.stop(this)
            else
                RestroomAvailabilityService.start(this)
            updateSubscribeButtonState(!started)
        }
        updateSubscribeButtonState(RestroomAvailabilityService.started)
    }

    private fun updateSubscribeButtonState(availabilityServiceStarted: Boolean) {
        mSubscribeButton.text = getString(if (availabilityServiceStarted) R.string.unsubscribe else R.string.subscribe)
    }

    override fun onResume() {
        super.onResume()
        updateAvailabilityUi(RestroomAvailabilityRepository.restroomAvailable)
        RestroomAvailabilityRepository.subscribe(this)
    }

    override fun onAvailabilityChange(isAvailable: Boolean) {
        updateAvailabilityUi(isAvailable)
    }

    private fun updateAvailabilityUi(available: Boolean) {
        if (available) {
            mRestroomAvailabilityLabel.setText(R.string.available)
            mRestroomAvailabilityLabel.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        } else {
            mRestroomAvailabilityLabel.setText(R.string.unavailable)
            mRestroomAvailabilityLabel.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
    }


    override fun onPause() {
        RestroomAvailabilityRepository.unsubscribe(this)
        super.onPause()
    }


}
