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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import org.json.JSONObject
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var text1: TextView
    private lateinit var testButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var accelerometer: Sensor ?= null
    private var resume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // nastavi accelometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        testButton = binding.testButton
        text1 = binding.text1

        testButton.setOnClickListener{
            getCurrentLocation { roadData ->
                Log.i("LEO123", roadData.latitude)
                postToWeb(roadData)
            }
        }
    }

    private fun postToWeb(roadData: RoadData){
        // Volley
        val volleyQueue = Volley.newRequestQueue(this)
        //val url = "http://34.65.105.245:3000/test"
        val url = "http://192.168.1.100:3000/test"

        val jsonObject = JSONObject()
        jsonObject.put("test", "androidTest")

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

    private fun getCurrentLocation(callback: (RoadData) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val loc = RoadData()
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    loc.latitude = location?.latitude.toString()
                    loc.longitude = location?.longitude.toString()
                    callback(loc)
                }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
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

    fun resumeReading(view: View){
        this.resume = true
    }

    fun stopReading(view: View){
        this.resume = false
    }

}