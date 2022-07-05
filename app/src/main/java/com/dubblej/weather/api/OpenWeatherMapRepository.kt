package com.dubblej.weather.api

import com.dubblej.weather.data.CurrentWeather
import com.dubblej.weather.data.OneCall
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenWeatherMapRepository {
    private val openWeatherMapApi: OpenWeatherMapApi

    init {
         val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openWeatherMapApi = retrofit.create(OpenWeatherMapApi::class.java)
    }

    suspend fun getCurrentWeatherFromCityName(city: String) : CurrentWeather {
        return openWeatherMapApi.getCurrentWeatherFromCityName(city)
    }

    suspend fun getCurrentWeatherFromLocation(latitude: String, longitude: String) : CurrentWeather {
        return openWeatherMapApi.getCurrentWeatherFromLocation(latitude, longitude)
    }

    suspend fun getOneCall(latitude: String, longitude: String) : OneCall {
        return openWeatherMapApi.getOneCall(latitude, longitude)
    }

}