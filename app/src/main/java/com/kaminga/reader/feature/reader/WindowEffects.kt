package com.kaminga.reader.feature.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

internal fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/** وقتی نوارهای مطالعه‌گر پنهان‌اند، نوار وضعیت و ناوبری سیستم هم پنهان می‌شوند. */
@Composable
internal fun ImmersiveEffect(hideBars: Boolean) {
    val view = LocalView.current
    val window = view.context.findActivity()?.window

    DisposableEffect(window, hideBars) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hideBars) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose { }
    }

    DisposableEffect(window) {
        onDispose {
            if (window != null) {
                WindowCompat.getInsetsController(window, view).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

/**
 * روشنایی همین پنجره را (در محدوده‌ی مجاز سیستم) تغییر می‌دهد؛ مقدار منفی یعنی پیش‌فرض سیستم.
 * به مجوز تنظیمات سیستم نیازی ندارد و با خروج از مطالعه‌گر به حالت اول برمی‌گردد.
 */
@Composable
internal fun BrightnessEffect(brightness: Float) {
    val window = LocalView.current.context.findActivity()?.window

    DisposableEffect(window, brightness) {
        if (window != null) {
            val params = window.attributes
            params.screenBrightness = brightness
            window.attributes = params
        }
        onDispose { }
    }

    DisposableEffect(window) {
        onDispose {
            if (window != null) {
                val params = window.attributes
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = params
            }
        }
    }
}
