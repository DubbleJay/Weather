package com.dubblej.weather.data

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    val name: String,
    @SerializedName("coord")
    val coordinates: Coordinates,
    val main: Main,
    val weather: List<Weather>,
    val timezone: Long,
    val wind: Wind,
    val clouds: Clouds,
    val visibility: Int,
    val sys: Sys
) {

    data class Coordinates(
        val lat: Double,
        val lon: Double
    )

    data class Main (
        val temp: Double,
        @SerializedName("feels_like")
        val feelsLike: Double,
        val humidity: Int
    )

    data class Weather(
        val description: String,
        val icon: String
    )

    data class Wind(
        val speed: Double
    )

    data class Clouds(
        val all: Int
    )

    data class Sys(
        val sunrise: Long,
        val sunset: Long
    )

}