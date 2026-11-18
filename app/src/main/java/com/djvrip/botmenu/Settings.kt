package com.djvrip.botmenu

// shared state between the overlay menu (writes) and the tap service (reads)
object Settings {
    @Volatile var autoTap = false
    @Volatile var tapX = -1          // screen px, -1 = not set yet
    @Volatile var tapY = -1
    @Volatile var ratePerSec = 8     // taps per second
    @Volatile var jitterMs = 40      // random +/- on each delay, so the taps are not perfectly regular
}
