package com.aquaflow.waterreminder.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedbackHelper {

    /**
     * Triggers an immediate, subtle tactile haptic response when water is added.
     * Uses View haptic feedback first with FLAG_IGNORE_VIEW_SETTING, and seamlessly falls back
     * to system Vibrator service if the device/OEM suppressed View haptics.
     */
    fun performIntakeHaptic(context: Context, view: View?) {
        var viewHandled = false
        if (view != null) {
            try {
                view.isHapticFeedbackEnabled = true
                val flag = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
                viewHandled = view.performHapticFeedback(constant, flag)
            } catch (_: Exception) {
            }
        }

        // If view haptics did not execute (e.g. OEM ROM setting or system touch feedback disabled),
        // execute a crisp, subtle tactile click via the Vibrator service.
        if (!viewHandled) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(20L)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
