package com.example.randomdogimagegeneration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.randomdogimagegeneration.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerateDogs.setOnClickListener {
            startActivity(Intent(this, GenerateDogsActivity::class.java))
        }

        binding.btnMyGeneratedDogs.setOnClickListener {
            startActivity(Intent(this, RecentlyGeneratedDogsActivity::class.java))
        }
    }
}