package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnSignup.setOnClickListener {
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

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val userRecord = Firebase.firestore.collection("users").document(task.result.user!!.uid)
                        val map = hashMapOf<String, Any>()
                        map["email"] = email
                        map["uid"] = task.result.user!!.uid
                        map["role"] = "USER"
                        userRecord.set(map).addOnSuccessListener {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task1 ->
                                    if (task1.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success")
                                        Toast.makeText(baseContext, "Registered Successfully", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, DashboardActivity::class.java))
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task1.exception)
                                        Toast.makeText(
                                            baseContext,
                                            "Authentication failed.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }
                        }
                            .addOnFailureListener {
                                Toast.makeText(
                                    baseContext,
                                    "Failed to register",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        binding.btnLogin.setOnClickListener { finish() }
    }

    companion object {
        const val TAG = "SignUpActivity"
    }
}