package com.example.mojwctermin

import kotlin.collections.HashSet

object RestroomAvailabilityRepository {
    private val restroomAvailabilityObservers: MutableSet<OnRestroomAvailabilityChange> = HashSet()
    var restroomAvailable = false
        set(value) {
            restroomAvailabilityObservers.forEach { it.onAvailabilityChange(value) }
        }

    fun subscribe(onRestroomAvailabilityChange: OnRestroomAvailabilityChange) =
            restroomAvailabilityObservers.add(onRestroomAvailabilityChange)

    fun unsubscribe(onRestroomAvailabilityChange: OnRestroomAvailabilityChange) =
            restroomAvailabilityObservers.remove(onRestroomAvailabilityChange)


    interface OnRestroomAvailabilityChange {
        fun onAvailabilityChange(isAvailable: Boolean)
    }
}