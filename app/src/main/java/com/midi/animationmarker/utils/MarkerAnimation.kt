package com.midi.animationmarker.utils

import android.os.Handler
import android.os.SystemClock
import android.view.animation.AccelerateDecelerateInterpolator

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

object MarkerAnimation {

    fun animateMarkerToGB(
        marker: Marker,
        finalPosition: LatLng,
        locationInterpolator: LocationInterpolator
    ) {
        val startPosition = marker.position
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 2000f

        handler.post(object : Runnable {
            internal var elapsed: Long = 0
            internal var t: Float = 0.toFloat()
            internal var v: Float = 0.toFloat()

            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)

                marker.setPosition(locationInterpolator.interpolate(v, startPosition, finalPosition))

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    fun animateMarkerToGB(
        marker: Marker,
        finalPosition: LatLng,
        locationInterpolator: LocationInterpolator,
        bearing: Float
    ) {
        val startPosition = marker.position
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 2000f

        handler.post(object : Runnable {
            internal var elapsed: Long = 0
            internal var t: Float = 0.toFloat()
            internal var v: Float = 0.toFloat()

            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)

                marker.setPosition(locationInterpolator.interpolate(v, startPosition, finalPosition))
                marker.setRotation(bearing)
                marker.setZIndex(99F)

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }
}