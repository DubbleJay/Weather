package com.dubblej.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dubblej.weather.R
import com.dubblej.weather.data.Weather
import com.dubblej.weather.databinding.FragmentWeatherBinding
import com.dubblej.weather.ui.adapters.HourlyForecastAdapter
import com.google.android.material.snackbar.Snackbar

private const val TAG = "PERMISSION_TAG"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

class WeatherFragment : Fragment() {

    private val viewModel : WeatherViewModel by activityViewModels()
    private lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherBinding.inflate(inflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            viewModel.refreshWeather()
        }

        viewModel.locationPermissionDenied.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                showPermissionDeniedSnackbar()
            }
        }

        viewModel.locationIsDisabled.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                showLocationDisabledSnackbar()
            }
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->

            binding.cityNameTextView.text = weather.city
            binding.weatherIcon.setImageResource(weather.icon)
            binding.descriptionTextView.text = weather.description

            binding.cloudsTextView.text = getString(R.string.current_clouds, weather.clouds)
            binding.visibilityTextView.text = getString(R.string.visibility, weather.visibility)
            binding.windSpeedTextView.text = getString(R.string.current_wind, weather.wind)
            binding.humidityTextView.text = getString(R.string.current_humidity, weather.humidity)
            binding.uvIndexTextView.text = getString(R.string.uv_index, weather.uvIndex)

            viewModel.tempScale.observe(viewLifecycleOwner) {

                if(it == Weather.TemperatureScale.FAHRENHEIT) {
                    binding.currentTemperatureTextView.text = getString(R.string.current_temperature, weather.currentTempFahrenheit, getString(R.string.degree_symbol_fahrenheit))
                    binding.feelsLikeTextView.text = getString(
                        R.string.feels_like,
                        weather.feelsLikeTempFahrenheit,
                        getString(R.string.degree_symbol_fahrenheit)
                    )
                    binding.dewPointTextView.text = getString(
                        R.string.dew_point,
                        weather.dewPointFahrenheit,
                        getString(R.string.degree_symbol_fahrenheit)
                    )
                }
                else {
                    binding.currentTemperatureTextView.text = getString(R.string.current_temperature, weather.currentTempCelsius, getString(R.string.degree_symbol_celsius))
                    binding.feelsLikeTextView.text = getString(
                        R.string.feels_like,
                        weather.feelsLikeTempCelsius,
                        getString(R.string.degree_symbol_celsius)
                    )
                    binding.dewPointTextView.text = getString(
                        R.string.dew_point,
                        weather.dewPointCelsius,
                        getString(R.string.degree_symbol_celsius)
                    )
                }
                val adapter = HourlyForecastAdapter(weather.hourlyForecast, it, viewModel.timeFormat.value!!)

                binding.hourlyForecastRecyclerView.adapter = adapter

                binding.dailyForecastLinearLayout.removeAllViews()

                for(day in weather.dailyForecast) {
                    val dailyForecastCardView = DailyForecastCardView(requireContext())
                    dailyForecastCardView.setDay(day, it, viewModel.timeFormat.value!!)
                    binding.dailyForecastLinearLayout.addView(dailyForecastCardView)
                }

            }

            viewModel.timeFormat.observe(viewLifecycleOwner) {
                if (it == Weather.TimeFormat.TWELVE_HOUR) {
                    binding.dateAndTimeTextView.text = weather.dateAndTime
                }
                else {
                    binding.dateAndTimeTextView.text = weather.militaryDateAndTime
                }

                val adapter = HourlyForecastAdapter(weather.hourlyForecast, viewModel.tempScale.value!!, it)

                binding.hourlyForecastRecyclerView.adapter = adapter

                binding.dailyForecastLinearLayout.removeAllViews()

                for(day in weather.dailyForecast) {
                    val dailyForecastCardView2 = DailyForecastCardView(requireContext())
                    dailyForecastCardView2.setDay(day, viewModel.tempScale.value!!, it)
                    binding.dailyForecastLinearLayout.addView(dailyForecastCardView2)
                }

            }

        }

        viewModel.status.observe(viewLifecycleOwner) { it ->

            binding.weatherScrollView.scrollTo(0, 0)
            binding.hourlyForecastRecyclerView.scrollTo(0, 0)

            when(it) {
                OpenWeatherApiStatus.LOADING -> {
                    binding.weatherContainer.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }

                OpenWeatherApiStatus.DONE -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.GONE
                    binding.weatherContainer.visibility = View.VISIBLE
                }

                OpenWeatherApiStatus.CITY_NOT_FOUND -> {
                    binding.progressBar.visibility = View.GONE
                    binding.weatherContainer.visibility = View.GONE
                    binding.errorTextView.text = getString(R.string.city_not_found)
                    binding.errorTextView.visibility = View.VISIBLE
                }

                OpenWeatherApiStatus.NO_INTERNET -> {
                    binding.progressBar.visibility = View.GONE
                    binding.weatherContainer.visibility = View.GONE
                    binding.errorTextView.text = getString(R.string.no_internet)
                    binding.errorTextView.visibility = View.VISIBLE
                }

                OpenWeatherApiStatus.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.weatherContainer.visibility = View.GONE
                    binding.errorTextView.text = getString(R.string.error)
                    binding.errorTextView.visibility = View.VISIBLE
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.enter_a_city)
        searchView.maxWidth = Int.MAX_VALUE

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_current_location) {

            if (checkPermission()) {
               if (isLocationEnabled(requireContext())) {
                   viewModel.getLocation()
               }
               else {
                   showLocationDisabledSnackbar()
               }
            }
            else {
                requestPermission()
            }
        }

        if (item.itemId == R.id.settings_menu_item) {
            val action = WeatherFragmentDirections.actionWeatherFragmentToSettingsFragment()
            findNavController().navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun checkPermission() : Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if(shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionDeniedSnackbar()
        }

        else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE)
            //ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                //REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE)
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    if (isLocationEnabled(requireContext())) {
                        viewModel.getLocation()
                    }
                    else {
                        showLocationDisabledSnackbar()
                    }


                else -> {
                    // Permission denied.
                    showPermissionDeniedSnackbar()
                }
            }
        }
    }

    private fun showPermissionDeniedSnackbar() {
        Snackbar.make(
            binding.fragmentWeather,
            R.string.permission_needed,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.settings) {
                // Build intent that displays the App settings screen.
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts(
                    "package",
                    requireActivity().packageName,
                    null
                )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .show()
    }

    private fun showLocationDisabledSnackbar() {
        Snackbar.make(binding.fragmentWeather, R.string.location_off,
            Snackbar.LENGTH_LONG).setAction(R.string.settings) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }.show()
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

}