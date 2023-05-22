package com.example.projektapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.Volley
import com.example.projektapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var text1: TextView
    private lateinit var testButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor ?= null
    private var resume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // nastavi accelometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        testButton = binding.testButton
        text1 = binding.text1

        testButton.setOnClickListener{ sendText() }
    }

    private fun sendText(){
        // Volley
        val volleyQueue = Volley.newRequestQueue(this)
        val url = "http://127.0.0.1:3000"

        Toast.makeText(this, "Text sent to website", Toast.LENGTH_SHORT).show()
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