package com.dubblej.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.dubblej.weather.UserPreferences

import com.dubblej.weather.api.OpenWeatherMapRepository
import com.dubblej.weather.data.Weather
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.net.UnknownHostException


enum class OpenWeatherApiStatus { LOADING, ERROR, DONE, CITY_NOT_FOUND, NO_INTERNET}
private enum class SearchMode {CURRENT_LOCATION, STRING_SEARCH}

class WeatherViewModel (private val app: Application): AndroidViewModel(app) {

    private var refreshMode = if (UserPreferences.getOnStartSearchPreference(app) == UserPreferences.OnStartSearchPreference.CURRENT_LOCATION)
        SearchMode.CURRENT_LOCATION else SearchMode.STRING_SEARCH

    private val _tempScale = MutableLiveData(UserPreferences.getTemperatureScale(app))
    val tempScale : LiveData<Weather.TemperatureScale> = _tempScale

    private val _userSearchPreference = MutableLiveData(UserPreferences.getOnStartSearchPreference(app))
    val userSearchPreference : LiveData<UserPreferences.OnStartSearchPreference> = _userSearchPreference

    private val _timeFormat = MutableLiveData(UserPreferences.getTimeFormat(app))
    val timeFormat : LiveData<Weather.TimeFormat> = _timeFormat

    private val _status = MutableLiveData<OpenWeatherApiStatus>()
    val status : LiveData<OpenWeatherApiStatus> = _status

    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather> = _weather

    private val _locationIsDisabled = MutableLiveData<Event<String>>()
    val locationIsDisabled : LiveData<Event<String>>
        get() = _locationIsDisabled

    private val _locationPermissionDenied = MutableLiveData<Event<String>>()
    val locationPermissionDenied : LiveData<Event<String>>
        get() = _locationPermissionDenied

    private var lastSearchTerm = UserPreferences.getStoredQuery(app)

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(app)
    private val locationRequest = LocationRequest.create().apply {
        interval = 4000
        fastestInterval = 4000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.equals(null)) {
                return
            }
            for (location in locationResult.locations) {
                Log.d("Location", "LocationResult: $location")
                getWeatherFromCurrentLocation(location.latitude.toString(), location.longitude.toString())
            }
            fusedLocationProviderClient.removeLocationUpdates(this)
        }
    }

    init {
        //check on start search preference
        if (UserPreferences.getOnStartSearchPreference(app) == UserPreferences.OnStartSearchPreference.CURRENT_LOCATION) {
            //check if location permission is granted
            if (ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //check if location is enabled on device
                if (isLocationEnabled(app)) {
                    //location is enabled
                    getLocation()
                }
                else {
                    //location is disabled
                    _locationIsDisabled.value = Event("")
                    getWeather(lastSearchTerm)
                }
            }
            else {
                _locationPermissionDenied.value = Event("")
                getWeather(lastSearchTerm)
                //UserPreferences.setOnStartSearchPreference(app, UserPreferences.OnStartSearchPreference.LAST_SEARCH_TERM)
            }
        }

        else if (UserPreferences.getOnStartSearchPreference(app) == UserPreferences.OnStartSearchPreference.LAST_SEARCH_TERM) {
            getWeather(lastSearchTerm)
        }

    }

    fun getWeather(city: String) {
        viewModelScope.launch {

            refreshMode = SearchMode.STRING_SEARCH
            _status.value = OpenWeatherApiStatus.LOADING

            try {
                val currentWeather = OpenWeatherMapRepository().getCurrentWeatherFromCityName(city)
                val lat = currentWeather.coordinates.lat
                val lon = currentWeather.coordinates.lon
                val oneCall = OpenWeatherMapRepository().getOneCall(lat.toString(), lon.toString())
                _weather.value = Weather(currentWeather, oneCall)
                lastSearchTerm = city
                UserPreferences.setStoredQuery(app, city)
                _status.value = OpenWeatherApiStatus.DONE

            } catch (exception: Exception) {
                handleException(exception)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        viewModelScope.launch {
            _status.value = OpenWeatherApiStatus.LOADING
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private fun getWeatherFromCurrentLocation(latitude: String, longitude: String) {
        viewModelScope.launch {
            refreshMode = SearchMode.CURRENT_LOCATION

            try {
                val currentWeather = OpenWeatherMapRepository().getCurrentWeatherFromLocation(latitude, longitude)
                val oneCall = OpenWeatherMapRepository().getOneCall(latitude, longitude)
                _weather.value = Weather(currentWeather, oneCall)
                _status.value = OpenWeatherApiStatus.DONE
            } catch (exception: Exception) {
                handleException(exception)
            }
        }
    }

    fun refreshWeather() {
        if (refreshMode == SearchMode.CURRENT_LOCATION) {
            if (ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled(app)) {
                   getLocation()
                }
                else {
                    _locationIsDisabled.value = Event("")
                }
            }
            else {
                _locationPermissionDenied.value = Event("")
            }
        }

        else {
            getWeather(lastSearchTerm)
        }
    }

    private fun handleException(exception: java.lang.Exception) {
        exception.printStackTrace()
        when (exception) {
            is retrofit2.HttpException -> _status.value = OpenWeatherApiStatus.CITY_NOT_FOUND
            is UnknownHostException -> _status.value = OpenWeatherApiStatus.NO_INTERNET
            else -> _status.value = OpenWeatherApiStatus.ERROR
        }
    }

    fun setTemperatureScale(tempScale : Weather.TemperatureScale) {
        UserPreferences.setTemperatureScale(app, tempScale)
        _tempScale.value = UserPreferences.getTemperatureScale(app)
    }

    fun setTimeFormat(timeFormat: Weather.TimeFormat) {
        UserPreferences.setTimeFormat(app, timeFormat)
        _timeFormat.value = UserPreferences.getTimeFormat(app)
    }

    fun setUserSearchPreference(onStartSearchPreference: UserPreferences.OnStartSearchPreference) {
        UserPreferences.setOnStartSearchPreference(app, onStartSearchPreference)
        _userSearchPreference.value = UserPreferences.getOnStartSearchPreference(app)
    }

    private fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            val locationManager = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            locationManager.isLocationEnabled
        } else {
            // This is Deprecated in API 28
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    open class Event<out T>(private val content: T) {

        var hasBeenHandled = false
            private set // Allow external read but not write

        /**
         * Returns the content and prevents its use again.
         */
        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        /**
         * Returns the content, even if it's already been handled.
         */
        fun peekContent(): T = content
    }
}