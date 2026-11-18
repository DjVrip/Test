package com.djvrip.botmenu

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import kotlin.random.Random

class TapService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() {
            if (Settings.autoTap && Settings.tapX >= 0) {
                tapOnce()
            }
            val base = 1000L / Settings.ratePerSec.coerceAtLeast(1)
            val jit = Settings.jitterMs.toLong()
            val delay = (base + if (jit > 0) Random.nextLong(-jit, jit + 1) else 0L).coerceAtLeast(20L)
            handler.postDelayed(this, delay)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.post(tick)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun tapOnce() {
        // a few px of random offset on every tap, so the position is not a single pixel
        val x = (Settings.tapX + Random.nextInt(-3, 4)).toFloat()
        val y = (Settings.tapY + Random.nextInt(-3, 4)).toFloat()
        val path = Path()
        path.moveTo(x, y)
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 30))
            .build()
        dispatchGesture(gesture, null, null)
    }
}
