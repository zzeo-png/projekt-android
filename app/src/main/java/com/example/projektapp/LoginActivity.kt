package com.example.projektapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.example.projektapp.databinding.ActivityLoginBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginButton: Button
    private lateinit var legacyLoginButton: TextView
    private lateinit var registerButton: TextView
    private lateinit var photoFile: File
    private lateinit var photoPath: String
    private lateinit var uri: Uri
    private var imageData: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginButton = binding.btnLogin
        legacyLoginButton = binding.btnLegacyLogin
        registerButton = binding.btnRegister

        loginButton.setOnClickListener { login() }
        legacyLoginButton.setOnClickListener { legacyLogin() }
        registerButton.setOnClickListener { register() }

        val sharedPreferences = getSharedPreferences("CestPrefs", Context.MODE_PRIVATE)
        val lastActivity = sharedPreferences.getString("LastActivity", "")
        Log.i("LEO123", lastActivity.toString())
        if(lastActivity != null && lastActivity.isNotEmpty()){
            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }

    private fun login(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            takePhoto()
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1002)
        }
    }

    private fun legacyLogin(){
        val llogin = Intent(this, LegacyLogin::class.java)
        startActivity(llogin)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun register(){
        val register = Intent(this, Register::class.java)
        startActivity(register)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun takePhoto() {
        photoFile = createImageFile()
        try{
            uri = FileProvider.getUriForFile(this, "com.example.projektapp.fileprovider", photoFile)
        } catch (e: java.lang.Exception){
            Log.e("LEO123", "Error: ${e.message}")
        }

        getcameraImage.launch(uri)
    }

    private val getcameraImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if(success){
            //Log.i("LEO123", "Image Location: $uri")
            createImageData(uri)
            sendPhotoToWeb(photoFile)
        }
        else{
            Log.e("LEO123", "Image not saved: $uri")
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("ddMMyyyy_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            photoPath = absolutePath
        }
    }

    private fun sendPhotoToWeb(photo: File){
        if(imageData == null){
            //Log.e("LEO123", "IMAGE DATA IS NULL")
            return
        }
        //Log.i("LEO123", "Sending image...")

        val volleyQueue = Volley.newRequestQueue(this)
        val url = "http://34.65.105.245:3001/login"
        //val url = "http://192.168.1.100:3001/login"

        val request = object : VolleyMultipartRequest(
            Method.POST, url,
            Response.Listener { response ->
                // Handle response
                val resData = String(response.data)
                val jsonRes = JSONObject(resData)
                handleResponse(jsonRes)
            },
            Response.ErrorListener {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params = HashMap<String, FileDataPart>()
                params["imageFile"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }
        }
        volleyQueue.add(request)
    }

    private fun createImageData(uri: Uri){
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use{
            imageData = it.readBytes()
        }
        Log.i("LEO123", "Creating image data...")
    }

    private fun handleResponse(res: JSONObject){
        if (res.getString("status") == "valid"){
            Log.i("LEO123", res.toString())
        }
        else{
            Toast.makeText(this, "Uporabnik ni zaznan", Toast.LENGTH_SHORT).show()
            Log.i("LEO123", res.toString())
        }
    }
}