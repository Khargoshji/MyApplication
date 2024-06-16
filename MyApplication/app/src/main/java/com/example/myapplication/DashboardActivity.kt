package com.example.myapplication

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.myapplication.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null
    private var role: String? = null
    private val pictureContact = registerForActivityResult(ActivityResultContracts.TakePicture()){taken->
        if (taken && imageUri != null){
            startActivity(Intent(this, SendPictureActivity::class.java)
                .putExtra("imageUri", imageUri.toString()))
        }
    }
    private val permissionContact = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        role = intent.getStringExtra("role")

        setContentView(binding.root)
        permissionContact.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, CAMERA, READ_MEDIA_IMAGES, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
        auth = Firebase.auth
        val directory = File(filesDir, "camera_images")
        if(!directory.exists()){
            directory.mkdirs()
        }
        val file = File(directory,"${Calendar.getInstance().timeInMillis}.png")
        imageUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)

        binding.toolbar.setOnMenuItemClickListener {menuItem ->
            if (menuItem.itemId == R.id.menu_logout){
                auth.signOut()

                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })

                return@setOnMenuItemClickListener true
            } else return@setOnMenuItemClickListener false
        }

        binding.cvTakePicture.setOnClickListener {
            pictureContact.launch(imageUri)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.secondary_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        val uid = Firebase.auth.currentUser!!.uid
        val dbRef = Firebase.firestore.collection("garbage-pics")
        dbRef
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener {querySnap->
                val list = ArrayList<GarbageModel>()

                querySnap.documents.forEach {docSnap->
                    docSnap.toObject(GarbageModel::class.java)?.let { list.add(it) }
                }

                val adapter = Adapter(list)
                binding.recyclerView.adapter = adapter

            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed to get list", Toast.LENGTH_SHORT).show()
            }

    }
}