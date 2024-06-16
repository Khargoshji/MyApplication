package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            if (binding.etEmail.text.isNullOrEmpty()){
                binding.etEmail.error = "Please enter email"
                return@setOnClickListener
            }

            if (binding.etPassword.text.isNullOrEmpty()){
                binding.etPassword.error = "Please enter password"
                return@setOnClickListener
            }

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        Firebase.firestore.collection("users").document(task.result.user!!.uid)
                            .get()
                            .addOnSuccessListener {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(SignUpActivity.TAG, "signInWithEmail:success")
                                Toast.makeText(baseContext, "Login Successfully", Toast.LENGTH_SHORT).show()

                                if (it.get("role") as String == "ADMIN")
                                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                                else startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                it.printStackTrace()
                                Toast.makeText(
                                    baseContext,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(SignUpActivity.TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}