package com.dubblej.weather.data

import com.dubblej.weather.R
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private const val DATE = "EEEE, MMM d"
private const val TIME = "h:mm aaa"
private const val MILITARY_TIME = "HH:mm"
private const val DATE_AND_TIME = "EEEE, MMMM d, h:mm aaa"
private const val MILITARY_DATE_AND_TIME = "EEEE, MMMM d, HH:mm"
private const val HOUR = "haaa"
private const val MILITARY_HOUR = "HH:mm"

data class Weather (private val currentWeather: CurrentWeather,
                    private val oneCall: OneCall) {

    enum class TimeFormat {TWELVE_HOUR, TWENTY_FOUR_HOUR}
    enum class TemperatureScale{FAHRENHEIT, CELSIUS}

    val city = currentWeather.name
    val dateAndTime = formatTime(oneCall.current.dt,
        oneCall.timezoneOffset, DATE_AND_TIME)
    val militaryDateAndTime = formatTime(oneCall.current.dt,
        oneCall.timezoneOffset, MILITARY_DATE_AND_TIME)
    val icon = getIcon(currentWeather.weather[0].icon)
    val description = currentWeather.weather[0].description.replaceFirstChar {it.uppercase()}
    val currentTempFahrenheit = kelvinToFahrenheit(currentWeather.main.temp)
    val currentTempCelsius =kelvinToCelsius(currentWeather.main.temp)
    val feelsLikeTempFahrenheit = kelvinToFahrenheit(currentWeather.main.feelsLike)
    val feelsLikeTempCelsius = kelvinToCelsius(currentWeather.main.feelsLike)

    private val dec = DecimalFormat("#,###.#")
    val visibility: String = dec.format(currentWeather.visibility / 1609.344)
    val humidity = currentWeather.main.humidity
    val clouds = currentWeather.clouds.all
    val wind : String = dec.format(currentWeather.wind.speed * 2.237)
    val dewPointFahrenheit = kelvinToFahrenheit(oneCall.current.dewPoint)
    val dewPointCelsius = kelvinToCelsius(oneCall.current.dewPoint)
    val uvIndex = oneCall.current.uvIndex

    val hourlyForecast = setHourlyForecast()
    val dailyForecast = setDailyForecast()

    private fun setHourlyForecast() : List<Hour> {
        val hours = mutableListOf<Hour>()
        for (hour in oneCall.hourly) {
            hours.add(Hour(hour))
        }
        return hours
    }

    private fun setDailyForecast() : List<Day> {
        val days = mutableListOf<Day>()
        for (day in oneCall.daily) {
            days.add(Day(day))
        }
        return days
    }

    private fun kelvinToFahrenheit(kelvinTemp: Double) : Int =
        (kelvinTemp - 273.15).roundToInt() * 9 / 5 + 32

    private fun kelvinToCelsius(kelvinTemp: Double) : Int =
        (kelvinTemp - 273.15).roundToInt()

    private fun formatTime(unixTimeStamp: Long, timeZoneOffset: Long, pattern: String) : String {
        val dateFormat: DateFormat =
            SimpleDateFormat(pattern, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateAndTime = Date((unixTimeStamp + timeZoneOffset) * 1000)
        return dateFormat.format(dateAndTime)
    }

    private fun getIcon(iconId: String): Int {
        return when (iconId) {
            "01d" -> R.drawable._01d
            "01n" -> R.drawable._01n
            "02d" -> R.drawable._02d
            "02n" -> R.drawable._02n
            "03d" -> R.drawable._03d
            "03n" -> R.drawable._03n
            "04d" -> R.drawable._04d
            "04n" -> R.drawable._04n
            "09d" -> R.drawable._09d
            "09n" -> R.drawable._09n
            "10d" -> R.drawable._10d
            "10n" -> R.drawable._10n
            "11d" -> R.drawable._11d
            "11n" -> R.drawable._11n
            "13d" -> R.drawable._13d
            "13n" -> R.drawable._13n
            "50d" -> R.drawable._50d
            "50n" -> R.drawable._50n
            else -> R.drawable._09d
        }
    }

    inner class Hour(hourly: OneCall.Hourly) {
        val time = formatTime(hourly.dt, oneCall.timezoneOffset, HOUR).lowercase()
        val militaryTime = formatTime(hourly.dt, oneCall.timezoneOffset, MILITARY_HOUR)
        val tempFahrenheit = kelvinToFahrenheit(hourly.temp)
        val tempCelsius = kelvinToCelsius(hourly.temp)
        val weatherIcon = getIcon(hourly.hourlyWeatherArray[0].icon)
        val pop = (hourly.probabilityOfPrecipitation * 100).toInt()
    }

    inner class Day(daily: OneCall.Daily) {
        val date = formatTime(daily.dt, oneCall.timezoneOffset, DATE)
        val description = daily.dailyWeatherArray[0].description.replaceFirstChar { it.uppercase()}
        val weatherIcon = getIcon(daily.dailyWeatherArray[0].icon)
        val highTempFahrenheit = kelvinToFahrenheit(daily.temp.max)
        val highTempCelsius = kelvinToCelsius(daily.temp.max)
        val lowTempFahrenheit = kelvinToFahrenheit(daily.temp.min)
        val lowTempCelsius = kelvinToCelsius(daily.temp.min)
        val humidity = daily.humidity
        val clouds = daily.clouds
        val pop = (daily.probabilityOfPrecipitation * 100).toInt()
        val wind : String = dec.format(daily.windSpeed * 2.237)
        val sunrise = formatTime(daily.sunrise, oneCall.timezoneOffset, TIME)
        val sunriseMilitaryTime = formatTime(daily.sunrise, oneCall.timezoneOffset, MILITARY_TIME)
        val sunset = formatTime(daily.sunset, oneCall.timezoneOffset, TIME)
        val sunsetMilitaryTime = formatTime(daily.sunset, oneCall.timezoneOffset, MILITARY_TIME)
    }

}