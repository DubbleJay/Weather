package com.dubblej.weather.data

import com.google.gson.annotations.SerializedName

data class OneCall(
    @SerializedName("timezone_offset")
    val timezoneOffset: Long,
    val current: Current,
    val hourly: List<Hourly>,
    val daily: List<Daily>
    )
{
    data class Current(
        val dt: Long,
        val sunrise: Long,
        val sunset: Long,
        @SerializedName("dew_point")
        val dewPoint: Double,
        @SerializedName("uvi")
        val uvIndex: Double
    )

    data class Hourly(
        val dt: Long,
        val temp: Double,
        @SerializedName("pop")
        val probabilityOfPrecipitation: Double,
        @SerializedName("weather")
        val hourlyWeatherArray: List<HourlyWeather>,
    ) {
        data class HourlyWeather(
            val icon: String
        )
    }

    data class Daily(
        val temp: Temp,
        val dt: Long,
        val sunrise: Long,
        val sunset: Long,
        val humidity: Int,
        @SerializedName("wind_speed")
        val windSpeed: Double,
        val clouds: Int,
        @SerializedName("weather")
        val dailyWeatherArray: List<DailyWeather>,
        @SerializedName("pop")
        val probabilityOfPrecipitation: Double
    ) {
        data class Temp(
            val night: Double,
            val day: Double,
            val min: Double,
            val max: Double,
            val morn: Double,
            val eve: Double
        )

        data class DailyWeather(
            val description: String,
            val icon: String
        )
    }
}