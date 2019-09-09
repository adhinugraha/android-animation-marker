package com.midi.animationmarker.activities


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.midi.animationmarker.R
import com.midi.animationmarker.utils.LocationInterpolator
import com.midi.animationmarker.utils.MarkerAnimation

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445

    private val defaultLocation = LatLng(-6.175349, 106.827152)
    private val pointLocation = LatLng(-6.153629, 106.726565)

    private var mMap: GoogleMap? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationMarker: Marker? = null
    private var mLastLocation: Location? = null
    private var firstTimeFlag = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMap()
    }

    private fun setupMap() {
        mMap!!.setMinZoomPreference(8F)
        mMap!!.setMaxZoomPreference(19F)

        val markerPoint = MarkerOptions()
        markerPoint.position(pointLocation)
        markerPoint.title("Point in Location")

        mMap!!.addMarker(markerPoint)
    }

    private fun startCurrentLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MapsActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
                return
            }
        }
        mFusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (ConnectionResult.SUCCESS == status){
            return true
        }else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(
                    this,
                    "Please Install google play services to use this application",
                    Toast.LENGTH_LONG
                ).show()

            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "Permission denied by uses", Toast.LENGTH_SHORT).show()
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startCurrentLocationUpdates()
            }
        }
    }

    private fun animateCamera(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun showMarker(currentLocation: Location, bearing: Float) {
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        if (mLocationMarker == null){
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.rotation(bearing)
            markerOptions.anchor(0.5F,0.5F)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_driver))

            mLocationMarker = mMap!!.addMarker(
                markerOptions
            )
        } else{
            MarkerAnimation.animateMarkerToGB(
                mLocationMarker!!,
                latLng,
                LocationInterpolator.Spherical(),
                bearing
            )
        }
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            if (locationResult!!.lastLocation == null)
                return

            mLastLocation = locationResult.lastLocation

            val locationList = locationResult.locations

            if (firstTimeFlag && mMap != null) {
                animateCamera(mLastLocation!!)
                firstTimeFlag = false
            }

            val location = locationList[locationList.size - 1]
            val mBearing = location.bearing

            showMarker(mLastLocation!!, mBearing)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mFusedLocationClient != null)
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (isGooglePlayServicesAvailable()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            startCurrentLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationClient = null
        mMap = null
    }
}
