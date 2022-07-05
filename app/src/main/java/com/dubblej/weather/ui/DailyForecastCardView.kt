package com.dubblej.weather.ui

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.dubblej.weather.R
import com.dubblej.weather.data.Weather
import com.dubblej.weather.databinding.CardViewDailyForecastBinding

class DailyForecastCardView constructor(context: Context, attrs: AttributeSet? = null,
                                        defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr){

    private val binding = CardViewDailyForecastBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setOnClickListener {
            expand()
        }

        binding.layout.layoutTransition.enableTransitionType(LayoutTransition.CHANGE_APPEARING)
    }

    fun setDay(day : Weather.Day, temperatureScale: Weather.TemperatureScale, timeFormat: Weather.TimeFormat) {
        //top half of card
        binding.dateTextView.text = day.date
        binding.forecastedDayDescriptionTextView.text = day.description
        binding.forecastedDayImageView.setImageResource(day.weatherIcon)
        if (temperatureScale == Weather.TemperatureScale.FAHRENHEIT) {
            binding.forecastedHighTextView.text = context.getString(
                R.string.high, day.highTempFahrenheit, context.getString(
                    R.string.degree_symbol
                )
            )

            binding.forecastedLowTextView.text = context.getString(
                R.string.low, day.lowTempFahrenheit, context.getString(
                    R.string.degree_symbol
                )
            )
        }
        else {
            binding.forecastedHighTextView.text = context.getString(
                R.string.high, day.highTempCelsius, context.getString(
                    R.string.degree_symbol
                )
            )

            binding.forecastedLowTextView.text = context.getString(
                R.string.low, day.lowTempCelsius, context.getString(
                    R.string.degree_symbol
                )
            )
        }

        //second half of card
        binding.humidityProgressBar.progress = day.humidity
        binding.humidityTextViewProgress.text = context.getString(R.string.percent_symbol, day.humidity)

        binding.cloudsProgressBar.progress = day.clouds
        binding.cloudsTextViewProgress.text = context.getString(R.string.percent_symbol, day.clouds)

        binding.precipitationProgressBar.progress = day.pop
        binding.precipitationTextViewProgress.text = context.getString(R.string.percent_symbol, day.pop)
        if(day.pop > 40)
            binding.precipitationTextViewProgress.setTextColor(Color.parseColor("#03b1fc"))

        binding.windSpeedTextView.text = context.getString(R.string.wind, day.wind)

        if (timeFormat == Weather.TimeFormat.TWELVE_HOUR) {
            binding.sunriseTextView.text = day.sunrise
            binding.sunsetTextView.text = day.sunset
        }
        else {
            binding.sunriseTextView.text = day.sunriseMilitaryTime
            binding.sunsetTextView.text = day.sunsetMilitaryTime
        }

    }

    private fun expand() {
        val v = if(binding.detailsGridLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        binding.detailsGridLayout.visibility = v
        binding.detailsIndicator.setImageResource(if (v == View.GONE) R.drawable.expand_more else R.drawable.expand_less)
        TransitionManager.beginDelayedTransition(binding.layout, AutoTransition())
    }


}