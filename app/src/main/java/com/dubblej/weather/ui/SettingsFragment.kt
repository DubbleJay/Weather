package com.dubblej.weather.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dubblej.weather.R
import com.dubblej.weather.UserPreferences
import com.dubblej.weather.data.Weather
import com.dubblej.weather.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

private const val TAG = "PERMISSION_TAG"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

class SettingsFragment : Fragment() {

    private val viewModel : WeatherViewModel by activityViewModels()
    private lateinit var binding : FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tempScale.observe(viewLifecycleOwner) {
            binding.tempScaleRadioGroup.check(
                if (it == Weather.TemperatureScale.FAHRENHEIT)
                    R.id.radio_fahrenheit else R.id.radio_celsius
            )
        }

        binding.tempScaleRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            when (checkedId) {
                R.id.radio_fahrenheit -> viewModel.setTemperatureScale(Weather.TemperatureScale.FAHRENHEIT)
                else -> viewModel.setTemperatureScale(Weather.TemperatureScale.CELSIUS)

            }
        }

        viewModel.timeFormat.observe(viewLifecycleOwner) {
            binding.timeFormatRadioGroup.check(
                if (it == Weather.TimeFormat.TWELVE_HOUR)
                    R.id.radio_12_hour else R.id.radio_24_hour
            )
        }

        binding.timeFormatRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_12_hour)
                viewModel.setTimeFormat(Weather.TimeFormat.TWELVE_HOUR)
            else
                viewModel.setTimeFormat(Weather.TimeFormat.TWENTY_FOUR_HOUR)
        }

        viewModel.userSearchPreference.observe(viewLifecycleOwner) {
            binding.radioGroupSearch.check(
                if (it == UserPreferences.OnStartSearchPreference.CURRENT_LOCATION)
                    R.id.radio_current_location else R.id.radio_last_search_term
            )
        }

        binding.radioGroupSearch.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_current_location -> {
                    if(checkPermission()) {
                        viewModel.setUserSearchPreference(UserPreferences.OnStartSearchPreference.CURRENT_LOCATION)
                    }
                    else {
                        viewModel.setUserSearchPreference(UserPreferences.getOnStartSearchPreference(requireContext()))
                        requestPermission()
                    }
                }
                R.id.radio_last_search_term -> viewModel.setUserSearchPreference(UserPreferences.OnStartSearchPreference.LAST_SEARCH_TERM)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (UserPreferences.getOnStartSearchPreference(requireContext()) == UserPreferences.OnStartSearchPreference.CURRENT_LOCATION) {
            if(!checkPermission()) {
                viewModel.setUserSearchPreference(UserPreferences.OnStartSearchPreference.LAST_SEARCH_TERM)
            }
        }
    }
    private fun checkPermission() : Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Snackbar.make(binding.fragmentSettings, R.string.permission_needed, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings) {
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
                }.show()
        }

        else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE)
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
                        viewModel.setUserSearchPreference(UserPreferences.OnStartSearchPreference.CURRENT_LOCATION)
                    }
                    else {
                        Snackbar.make(binding.fragmentSettings, R.string.location_off,
                            Snackbar.LENGTH_LONG).setAction(R.string.settings) {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }.show()
                    }


                else -> {
                    // Permission denied.
                    Snackbar.make(
                        binding.fragmentSettings,
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
            }
        }
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