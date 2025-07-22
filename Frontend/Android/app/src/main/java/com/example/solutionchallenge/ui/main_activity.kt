package com.example.solutionchallenge.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.solutionchallenge.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment()) // 초기 프래그먼트
        }

        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment()) // 홈 프래그먼트
                    true
                }
                R.id.nav_camera -> {
                    replaceFragment(CameraMenuFragment()) // 카메라 프래그먼트
                    true
                }
                R.id.nav_archive -> {
                    replaceFragment(ArchiveFragment()) // 아카이브 프래그먼트
                    true
                }
                else -> false
            }
        }
    }
}
