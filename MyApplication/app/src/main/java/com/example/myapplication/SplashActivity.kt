package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth = Firebase.auth
        Handler(Looper.myLooper()!!).postDelayed({
            if (auth.currentUser != null){
                Firebase.firestore.collection("users").document(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener {
                        if (it.get("role") as String == "ADMIN")
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                        else startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }, 2000)
    }
}