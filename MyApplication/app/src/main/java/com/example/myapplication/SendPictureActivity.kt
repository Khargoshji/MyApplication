package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication.databinding.ActivitySendPictureBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.Locale


class SendPictureActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySendPictureBinding
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var fullAddress: String? = null

    private var storageReference: StorageReference? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCurrentLocation()
        val imageUri = intent.getStringExtra("imageUri")
        if (imageUri != null) {
            uri = Uri.parse(imageUri)
            binding.image.setImageURI(uri)
        } else {
            Toast.makeText(this, "Invalid image", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.sendButton.setOnClickListener {
            binding.sendButton.isEnabled = false
            uploadImage()
            binding.sendButton.isEnabled = false
        }

    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            mFusedLocationClient?.removeLocationUpdates(this)
            if (locationResult.lastLocation == null) {
                binding.tvLocation.text = "Unable to get current location"
                return
            }
            val location = locationResult.lastLocation!!
            latitude = location.latitude
            longitude = location.longitude
            getFullAddress()
        }
    }

    private fun getCurrentLocation() {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(5000 + 10000)
            .build()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun getFullAddress() {
        val address: List<Address>?
        val geocoder = Geocoder(this, Locale.getDefault())

        address = geocoder.getFromLocation(latitude!!, longitude!!, 1)
        val addresses = address!![0]
        val city = addresses.locality
        val state = addresses.adminArea
        val country = addresses.countryName
        val postalCode = addresses.postalCode
        val knownName = addresses.featureName



        fullAddress =
            "$knownName, $city, $state, $country, $postalCode \nLatitude: $latitude, Longitude: $longitude"
        binding.tvLocation.text = fullAddress
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationClient?.removeLocationUpdates(locationCallback)
    }


    private fun uploadImage() {
        storageReference = Firebase.storage.reference
        val uniqueImageName = "image_${System.currentTimeMillis()}"
        val store = storageReference!!.child("images").child(uniqueImageName)

        store.putFile(uri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        uploadData(downloadUri.toString())
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@SendPictureActivity,
                            "Download URL fetch failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@SendPictureActivity,
                    "Image upload failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun uploadData(imgUrl: String) {
        val user = Firebase.auth.currentUser
        val data = GarbageModel(
            imgUrl,
            user!!.uid,
            user.displayName,
            latitude,
            longitude,
            fullAddress
        )

        val fireStore = Firebase.firestore
        fireStore.collection("garbage-pics")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this@SendPictureActivity, "images uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("ERROR", "Error adding document", e)
            }

    }

}






