package com.djvrip.botmenu

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings as AndroidSettings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var menu: LinearLayout? = null
    private var picker: View? = null
    private lateinit var posLabel: TextView

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        if (!AndroidSettings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        showMenu()
    }

    private fun showMenu() {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setBackgroundColor(Color.argb(210, 20, 20, 20))
        box.setPadding(24, 24, 24, 24)

        val title = TextView(this)
        title.text = "bot menu"
        title.setTextColor(Color.WHITE)
        title.textSize = 16f
        box.addView(title)

        val sw = Switch(this)
        sw.text = "auto tap (attack)"
        sw.setTextColor(Color.WHITE)
        sw.isChecked = Settings.autoTap
        sw.setOnCheckedChangeListener { _, on -> Settings.autoTap = on }
        box.addView(sw)

        posLabel = TextView(this)
        posLabel.setTextColor(Color.WHITE)
        posLabel.text = posText()
        box.addView(posLabel)

        val setPos = Button(this)
        setPos.text = "set tap position"
        setPos.setOnClickListener { startPick() }
        box.addView(setPos)

        box.addView(label("taps per second"))
        box.addView(seek(1, 20, Settings.ratePerSec) { Settings.ratePerSec = it })

        box.addView(label("jitter ms"))
        box.addView(seek(0, 200, Settings.jitterMs) { Settings.jitterMs = it })

        val close = Button(this)
        close.text = "close"
        close.setOnClickListener { stopSelf() }
        box.addView(close)

        menu = box
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.TOP or Gravity.START
        lp.x = 40
        lp.y = 200
        wm.addView(box, lp)
    }

    private fun label(s: String): TextView {
        val t = TextView(this)
        t.text = s
        t.setTextColor(Color.WHITE)
        return t
    }

    private fun seek(lo: Int, hi: Int, value: Int, onChange: (Int) -> Unit): SeekBar {
        val sb = SeekBar(this)
        sb.max = hi - lo
        sb.progress = value - lo
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                onChange(progress + lo)
            }
            override fun onStartTrackingTouch(bar: SeekBar) {}
            override fun onStopTrackingTouch(bar: SeekBar) {}
        })
        return sb
    }

    private fun posText(): String =
        if (Settings.tapX < 0) "tap position: not set"
        else "tap position: ${Settings.tapX}, ${Settings.tapY}"

    // full-screen transparent layer: the next touch sets the tap position, then the menu comes back
    private fun startPick() {
        if (picker != null) return
        menu?.visibility = View.GONE
        val layer = View(this)
        layer.setBackgroundColor(Color.argb(60, 255, 0, 0))
        layer.setOnTouchListener { _, ev ->
            if (ev.action == MotionEvent.ACTION_UP) {
                Settings.tapX = ev.rawX.toInt()
                Settings.tapY = ev.rawY.toInt()
                wm.removeView(layer)
                picker = null
                menu?.visibility = View.VISIBLE
                posLabel.text = posText()
            }
            true
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        picker = layer
        wm.addView(layer, lp)
    }

    override fun onDestroy() {
        super.onDestroy()
        Settings.autoTap = false
        picker?.let { runCatching { wm.removeView(it) } }
        menu?.let { runCatching { wm.removeView(it) } }
    }
}
