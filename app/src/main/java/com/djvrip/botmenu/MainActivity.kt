package com.djvrip.botmenu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        info = TextView(this)
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(40, 40, 40, 40)

        val b1 = Button(this)
        b1.text = "1. autoriser l'overlay"
        b1.setOnClickListener { askOverlay() }

        val b2 = Button(this)
        b2.text = "2. lancer le menu"
        b2.setOnClickListener {
            if (AndroidSettings.canDrawOverlays(this)) {
                startService(Intent(this, OverlayService::class.java))
                info.text = "menu lancé"
            } else {
                info.text = "overlay pas autorisé (étape 1)"
            }
        }

        val b3 = Button(this)
        b3.text = "3. activer le service d'accessibilité"
        b3.setOnClickListener { startActivity(Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)) }

        box.addView(info)
        box.addView(b1)
        box.addView(b2)
        box.addView(b3)
        setContentView(box, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun askOverlay() {
        if (AndroidSettings.canDrawOverlays(this)) {
            info.text = "overlay ok"
        } else {
            startActivity(Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }
}
