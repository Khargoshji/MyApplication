package com.example.myapplication


data class GarbageModel(
    val imageUrl: String = "",
    val uid: String = "",
    val uName: String? = null,
    val lat: Double? = null,
    val log: Double? = null,
    val address: String? = null
)
