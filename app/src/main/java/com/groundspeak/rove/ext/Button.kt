package com.groundspeak.rove.ext

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.widget.Button

fun Button.enable() {
    isEnabled = true
    background.colorFilter = null
}

@Suppress("DEPRECATION")
fun Button.disable() {
    isEnabled = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        background.colorFilter = BlendModeColorFilter(Color.GRAY, BlendMode.MULTIPLY)
    } else {
        background.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
    }
}
