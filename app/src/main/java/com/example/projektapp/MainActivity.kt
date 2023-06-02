package com.example.projektapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.projektapp.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var text1: TextView
    private lateinit var startTracking: Button
    private lateinit var stopTracking: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var accelerometer: Sensor ?= null
    private var resume = false
    // minimalna razdalja meritve
    private val distanceInterval = 1f
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var startPosition: Location = Location("")
    private var endPosition: Location = Location("")
    private var startPositionDeferred = CompletableDeferred<Location>()
    private var endPositionDeferred = CompletableDeferred<Location>()
    private var trackingStatus: Boolean = false
    private var analyzingCoroutine: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // nastavi accelometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).apply {
            setMinUpdateDistanceMeters(distanceInterval)
            setGranularity(Granularity.GRANULARITY_FINE)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for(location in locationResult.locations){
                    Log.i("LEO123", "location update")
                    if(startPositionDeferred.isCompleted){
                        endPositionDeferred.complete(location)
                    }
                    else{
                        startPositionDeferred.complete(location)
                    }
                }
            }
        }

        startTracking = binding.startTracking
        stopTracking = binding.stopTracking
        text1 = binding.text1

        startTracking.setOnClickListener{
            startAnalyzing()
        }
        stopTracking.setOnClickListener{
            trackingStatus = false
            stopLocationUpdates()
            stopReading(findViewById(R.id.btn_startA))
            analyzingCoroutine?.cancel()
        }
    }

    private fun startAnalyzing(){
        trackingStatus = true
        analyzingCoroutine = CoroutineScope(Dispatchers.Main).launch {
            while(trackingStatus){
                // čakaj na začetno lokacijo
                Log.i("LEO123", "started...")
                //startPosition = getCurrentLocationAsync()

                // začni sledenje lokacije
                startLocationUpdates()

                if(!startPositionDeferred.isCompleted){
                    startPosition = startPositionDeferred.await()
                }
                resumeReading(findViewById(R.id.btn_startA))
                Log.i("LEO123", "got start position")

                // čakaj na končno lokacijo
                endPosition = endPositionDeferred.await()
                Log.i("LEO123", "got end position")

                // kalkuliraj podatke za stanje

                val data = RoadData(
                    startPosition.latitude,
                    startPosition.longitude,
                    endPosition.latitude,
                    endPosition.longitude,
                    0
                )

                // ponastavi točki
                startPosition = endPosition
                //startPositionDeferred = CompletableDeferred<Location>()
                endPositionDeferred = CompletableDeferred<Location>()

                // pošlji rezultate
                Log.i("LEO123", "Sending data...")
                postToWeb(data)
            }
        }
    }

    private fun postToWeb(roadData: RoadData){
        // Volley
        val volleyQueue = Volley.newRequestQueue(this)
        //val url = "http://34.65.105.245:3001/roads"
        val url = "http://192.168.1.100:3001/roads"

        val postRequest = object: StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                //Handle response
                val strResp = response.toString()
                Toast.makeText(this, strResp, Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getBody(): ByteArray {
                val gson = Gson()
                val jsonBody = gson.toJson(roadData)
                return jsonBody.toByteArray(Charset.defaultCharset())
            }
        }

        volleyQueue.add(postRequest)
    }

    private fun startLocationUpdates(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    private fun stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event != null && resume){
            if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
                text1.text = event.values[0].toString()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun resumeReading(view: View){
        this.resume = true
    }

    private fun stopReading(view: View){
        this.resume = false
    }

}