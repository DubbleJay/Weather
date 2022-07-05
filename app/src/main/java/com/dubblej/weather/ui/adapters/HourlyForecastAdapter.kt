package com.dubblej.weather.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dubblej.weather.UserPreferences
import com.dubblej.weather.R
import com.dubblej.weather.data.Weather
import com.dubblej.weather.databinding.ListItemHourlyForecastBinding

class HourlyForecastAdapter(private val hours: List<Weather.Hour>,
                            private val temperatureScale: Weather.TemperatureScale,
                            private val timeFormat: Weather.TimeFormat)

    : RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastAdapter.HourlyForecastViewHolder {
        val binding = ListItemHourlyForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HourlyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyForecastAdapter.HourlyForecastViewHolder, position: Int) {
        val forecastedHour = hours[position]
        holder.bindHour(forecastedHour)
    }

    override fun getItemCount(): Int {
        return hours.size
    }

    inner class HourlyForecastViewHolder(private val binding: ListItemHourlyForecastBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bindHour(forecastedHour: Weather.Hour) {
            val context = binding.root.context

            if (timeFormat == Weather.TimeFormat.TWELVE_HOUR)
                binding.timeTextView.text = forecastedHour.time
            else
                binding.timeTextView.text = forecastedHour.militaryTime

            if (temperatureScale == Weather.TemperatureScale.FAHRENHEIT)
                binding.hourlyTempTextView.text = context.getString(R.string.hourly_temperature, forecastedHour.tempFahrenheit)
            else
                binding.hourlyTempTextView.text = context.getString(R.string.hourly_temperature, forecastedHour.tempCelsius)

            binding.hourlyImageView.setImageResource(forecastedHour.weatherIcon)
            binding.hourlyPopTextView.text = context.getString(R.string.percent_symbol, forecastedHour.pop)

            binding.hourlyPopTextView.visibility =
                if(forecastedHour.pop >= 40 ) View.VISIBLE else View.INVISIBLE
        }
    }
}