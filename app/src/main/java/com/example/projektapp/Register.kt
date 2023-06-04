package com.example.projektapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.projektapp.databinding.ActivityRegisterBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import java.nio.charset.Charset

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerButton: Button
    private lateinit var homeButton: TextView

    private lateinit var input_username: EditText
    private lateinit var input_password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerButton = binding.RegisterBtn
        homeButton = binding.RegisterLoginBtn

        input_username = binding.RegUsername
        input_password = binding.RegPassword

        registerButton.setOnClickListener { postToWeb(input_username.text.toString(), input_password.text.toString()) }
        homeButton.setOnClickListener { home() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                home()
            }
        })
    }

    private fun home(){
        val login = Intent(this, LoginActivity::class.java)
        startActivity(login)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    private fun postToWeb(username: String, password: String){
        // Volley
        val volleyQueue = Volley.newRequestQueue(this)
        val url = "http://34.65.105.245:3001/register"
        //val url = "http://192.168.1.100:3001/register"

        val postRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                // Handle response
                handleResponse(response)
                //Log.i("LEO123", response.toString())
            },
            Response.ErrorListener {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getBody(): ByteArray {
                val gson = Gson()
                val jsonObject = JsonObject()
                jsonObject.addProperty("username", username)
                jsonObject.addProperty("password", password)
                val jsonBody = gson.toJson(jsonObject)

                return jsonBody.toByteArray(Charset.defaultCharset())
            }
        }

        volleyQueue.add(postRequest)
    }

    private fun handleResponse(res: String){
        val temp = JSONObject(res)
        if(temp.getString("status") == "valid"){
            home()
        }
        else if(temp.getString("status") == "exists"){
            Toast.makeText(this, "Uporabnik že obstaja", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Registracija ni uspela", Toast.LENGTH_SHORT).show()
        }
    }

}