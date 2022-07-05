package com.dubblej.weather.api

import com.dubblej.weather.data.CurrentWeather
import com.dubblej.weather.data.OneCall
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface OpenWeatherMapApi {

    @GET("weather")
    suspend fun getCurrentWeatherFromCityName(
        @Query("q") city: String,
        @Query("appid") key: String = "ce4bec34b6ca0e33511f4a259262267b"
    ) : CurrentWeather

    @GET("weather")
    suspend fun getCurrentWeatherFromLocation(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") key: String = "ce4bec34b6ca0e33511f4a259262267b"
    ) : CurrentWeather

    @GET("onecall")
    suspend fun getOneCall(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") key: String = "ce4bec34b6ca0e33511f4a259262267b"
    ): OneCall

}