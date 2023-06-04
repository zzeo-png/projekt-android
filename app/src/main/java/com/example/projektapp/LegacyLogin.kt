package com.example.projektapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.projektapp.databinding.ActivityLegacyLoginBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import java.nio.charset.Charset

class LegacyLogin : AppCompatActivity() {
    private lateinit var binding: ActivityLegacyLoginBinding
    private lateinit var lloginButton: Button
    private lateinit var backButton: TextView

    private lateinit var input_username: EditText
    private lateinit var input_password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLegacyLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lloginButton = binding.legacyLoginBtn
        backButton = binding.backBtn

        input_username = binding.username
        input_password = binding.password

        lloginButton.setOnClickListener { postToWeb(input_username.text.toString(), input_password.text.toString()) }
        backButton.setOnClickListener { back() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                back()
            }
        })
    }

    private fun postToWeb(username: String, password: String){
        // Volley
        val volleyQueue = Volley.newRequestQueue(this)
        val url = "http://34.65.105.245:3001/llogin"
        //val url = "http://192.168.1.100:3001/llogin"

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

    private fun back(){
        val login = Intent(this, LoginActivity::class.java)
        startActivity(login)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    private fun handleResponse(res: String){
        val temp = JSONObject(res)
        if(temp.getString("status") == "valid"){
            val mainActivity = Intent(this, MainActivity::class.java)
            mainActivity.putExtra("username", temp.getString("user"))
            startActivity(mainActivity)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
        else{
            Toast.makeText(this, "Prijava ni uspela", Toast.LENGTH_SHORT).show()
        }
    }
}