package com.dubblej.weather

import android.content.Context
import com.dubblej.weather.data.Weather

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_TEMPERATURE_SCALE = "temperatureScale"
private const val PREF_ON_START_SEARCH_PREF = "onStartSearch"
private const val PREF_TIME_FORMAT = "timeFormat"

object UserPreferences  {

    enum class OnStartSearchPreference  {CURRENT_LOCATION, LAST_SEARCH_TERM}

    fun getStoredQuery(context: Context): String {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return prefs.getString(PREF_SEARCH_QUERY, "Kotlin")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREF_SEARCH_QUERY, query)
            .apply()
    }

    fun getOnStartSearchPreference(context: Context) : OnStartSearchPreference {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return when (prefs.getInt(PREF_ON_START_SEARCH_PREF, 1)) {
            0 -> OnStartSearchPreference.CURRENT_LOCATION
            else -> OnStartSearchPreference.LAST_SEARCH_TERM
        }
    }

    fun setOnStartSearchPreference(context: Context, onStartSearchPreference: OnStartSearchPreference) {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val onStartSearchPreferenceNumber = when(onStartSearchPreference) {
            OnStartSearchPreference.CURRENT_LOCATION -> 0
            OnStartSearchPreference.LAST_SEARCH_TERM -> 1
        }
        prefs.edit()
            .putInt(PREF_ON_START_SEARCH_PREF, onStartSearchPreferenceNumber)
            .apply()
    }

    fun getTemperatureScale(context: Context): Weather.TemperatureScale {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return if (prefs.getInt(PREF_TEMPERATURE_SCALE, 0) == 0)  Weather.TemperatureScale.FAHRENHEIT
            else Weather.TemperatureScale.CELSIUS
    }

    fun setTemperatureScale(context: Context, temperatureScale: Weather.TemperatureScale)  {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val tempScaleNumber = if (temperatureScale == Weather.TemperatureScale.FAHRENHEIT) 0 else 1
        prefs.edit()
            .putInt(PREF_TEMPERATURE_SCALE, tempScaleNumber)
            .apply()
    }

    fun getTimeFormat(context: Context) : Weather.TimeFormat {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return if (prefs.getInt(PREF_TIME_FORMAT, 0) == 0)  Weather.TimeFormat.TWELVE_HOUR
        else Weather.TimeFormat.TWENTY_FOUR_HOUR
    }

    fun setTimeFormat(context: Context, timeFormat: Weather.TimeFormat) {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val tempScaleNumber = if (timeFormat == Weather.TimeFormat.TWELVE_HOUR) 0 else 1
        prefs.edit()
            .putInt(PREF_TIME_FORMAT, tempScaleNumber)
            .apply()
    }
}