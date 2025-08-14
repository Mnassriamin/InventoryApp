package com.example.elmnassri

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity // <-- Note the change here
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.elmnassri.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() { // <-- And here

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the navigation controller
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connect the BottomNavigationView to the NavController
        binding.bottomNavView.setupWithNavController(navController)
    }
}