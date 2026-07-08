package com.example.util

import android.content.Context

object BiometricHelper {
    private const val PREFS_NAME = "payoutly_security_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_IS_LOCKED = "app_is_locked"

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isAppLocked(context: Context): Boolean {
        // If biometric is enabled, the app should launch in locked state initially
        return isBiometricEnabled(context)
    }
}
