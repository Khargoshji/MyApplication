package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapplication.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setOnMenuItemClickListener {menuItem ->
            if (menuItem.itemId == R.id.menu_logout){
                Firebase.auth.signOut()

                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })

                return@setOnMenuItemClickListener true
            } else return@setOnMenuItemClickListener false
        }
    }

    override fun onStart() {
        super.onStart()

        val dbRef = Firebase.firestore.collection("garbage-pics")
        dbRef
            .get()
            .addOnSuccessListener {querySnap->
                val list = ArrayList<GarbageModel>()
                querySnap.documents.forEach {docSnap->
                    docSnap.toObject(GarbageModel::class.java)?.let { list.add(it) }
                }
                if (list.isEmpty()) Toast.makeText(this, "No pictures found", Toast.LENGTH_SHORT).show()

                val adapter = Adapter(list)
                binding.recyclerView.adapter = adapter

            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed to get list", Toast.LENGTH_SHORT).show()
            }
    }
}