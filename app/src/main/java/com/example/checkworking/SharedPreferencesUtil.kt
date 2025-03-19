package com.example.checkworking

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

object SharedPreferencesUtil {
    private const val PREF_NAME = "my_shared_prefs"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun putString(context: Context, key: String, value: String) {
        getPreferences(context).edit() { putString(key, value) }
    }

    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return getPreferences(context).getString(key, defaultValue) ?: defaultValue
    }

    fun putInt(context: Context, key: String, value: Int) {
        getPreferences(context).edit() { putInt(key, value) }
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return getPreferences(context).getInt(key, defaultValue)
    }

    fun putBoolean(context: Context, key: String, value: Boolean) {
        getPreferences(context).edit() { putBoolean(key, value) }
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getPreferences(context).getBoolean(key, defaultValue)
    }

    fun putFloat(context: Context, key: String, value: Float) {
        getPreferences(context).edit() { putFloat(key, value) }
    }

    fun getFloat(context: Context, key: String, defaultValue: Float = 0f): Float {
        return getPreferences(context).getFloat(key, defaultValue)
    }

    fun putLong(context: Context, key: String, value: Long) {
        getPreferences(context).edit() { putLong(key, value) }
    }

    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long {
        return getPreferences(context).getLong(key, defaultValue)
    }

    fun putStringSet(context: Context, key: String, values: Set<String>) {
        getPreferences(context).edit() { putStringSet(key, values) }
    }

    fun getStringSet(context: Context, key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return getPreferences(context).getStringSet(key, defaultValue) ?: defaultValue
    }

    fun remove(context: Context, key: String) {
        getPreferences(context).edit() { remove(key) }
    }

    fun clear(context: Context) {
        getPreferences(context).edit() { clear() }
    }
}
