package com.example.cutwaste.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticUtil {

    fun performLightHaptic(view: View?, context: Context?, enabled: Boolean) {
        if (!enabled) return
        try {
            if (view != null && view.isHapticFeedbackEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                return
            }
            vibrate(context, 15)
        } catch (_: Exception) {}
    }

    fun performMediumHaptic(view: View?, context: Context?, enabled: Boolean) {
        if (!enabled) return
        try {
            if (view != null && view.isHapticFeedbackEnabled) {
                view.performHapticFeedback(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        HapticFeedbackConstants.CONFIRM
                    } else {
                        HapticFeedbackConstants.LONG_PRESS
                    }
                )
                return
            }
            vibrate(context, 35)
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun vibrate(context: Context?, durationMs: Long) {
        if (context == null) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
