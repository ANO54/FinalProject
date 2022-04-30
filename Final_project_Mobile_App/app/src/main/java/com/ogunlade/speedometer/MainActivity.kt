package com.ogunlade.speedometer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
EasyPermissions.RationaleCallbacks
{
    private val TAG = "MainActivity"
    private val LOCATION_PERM = 124
    private var speedUpStartTime = 0L
    private var speedUpEndTime = 0L
    private var speedDownStartTime = 0L
    private var speedDownEndTime = 0L

    private lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var  locationRequest: LocationRequest
    private lateinit var  locationCallback: LocationCallback


    private var isDone  :Boolean by Delegates.observable(false){property, oldValue, newValue ->
        if(newValue == true)
        {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        askForLocationPermission()
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationRequest ?:  return
                if(!isDone){
                    val speedToInt = locationResult.lastLocation.speed.toInt()
                    calcSpeed(speedToInt)
                    currentSpeedId.text= speedToInt.toString()

                }
            }

        }


    }

    private fun calcSpeed(speed: Int) {

        if(speed >= 10){
            speedUpStartTime= System.currentTimeMillis()
            speedDownEndTime= System.currentTimeMillis()

            if (speedDownStartTime != 0L){
                val speedDownTime= speedDownEndTime - speedDownStartTime
                thirtyToTenId.text = (speedDownTime/1000).toString()
                speedDownStartTime= 0L
            }
        }
        else if (speed >=30)
        {
            if(speedUpStartTime != 0L){
                speedUpEndTime = System.currentTimeMillis()
                val speedUpTime= speedUpEndTime - speedUpStartTime
                tenToThirtyId.text= (speedUpTime/1000).toString()
                speedUpStartTime = 0L

            }
            speedDownStartTime=System.currentTimeMillis()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
           locationRequest,
           locationCallback,
           Looper.getMainLooper()
       )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }





    fun createLocationRequest() {
       locationRequest = LocationRequest.create().apply {
           interval = 1000
           priority = LocationRequest.PRIORITY_HIGH_ACCURACY

       }
    }

   fun askForLocationPermission()
   {
       if (hasLocationPermissions()) {
           if (ActivityCompat.checkSelfPermission(
                   this,
                   Manifest.permission.ACCESS_FINE_LOCATION
               ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                   this,
                   Manifest.permission.ACCESS_COARSE_LOCATION
               ) != PackageManager.PERMISSION_GRANTED
           ) {
               // TODO: Consider calling
               //    ActivityCompat#requestPermissions
               // here to request the missing permissions, and then overriding
               //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
               //                                          int[] grantResults)
               // to handle the case where the user grants the permission. See the documentation
               // for ActivityCompat#requestPermissions for more details.
               return
           }
           fusedLocationProviderClient.lastLocation
               .addOnSuccessListener { location: Location? ->
                   //do nothing
               }
       }
       else{
           EasyPermissions.requestPermissions(
               this,
               "Need permission to find your Location in other to get your speed",
               LOCATION_PERM,
               Manifest.permission.ACCESS_FINE_LOCATION
           )
       }



   }

    private fun hasLocationPermissions(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
        AppSettingsDialog.Builder(this).build().show()
    }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            val yes= "Allow"
            val no ="Deny"
            Toast.makeText(this,"onActivityResult",Toast.LENGTH_LONG).show()
        }


    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // It means nothing
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }





    }
