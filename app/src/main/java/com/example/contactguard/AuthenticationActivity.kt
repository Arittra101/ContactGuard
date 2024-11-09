package com.example.contactguard

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.contactguard.MainActivity.Companion.CONTACT_PERMISSION_CODE
import com.example.contactguard.databinding.ActivityAuthBinding
import com.example.contactguard.navigation.socialNavigationList
import com.google.android.material.tabs.TabLayoutMediator

class AuthenticationActivity : AppCompatActivity() {

    var pagerAdapter: ViewPagerAdapter? = null
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewpagerSetup()
        requestContactsPermission()

    }

    private fun viewpagerSetup() {
        pagerAdapter = ViewPagerAdapter(this)
        pagerAdapter?.apply {
            addIdentifiers(socialNavigationList)
        }
        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Unsync Contact"
                1 -> "Unsaved Contact"
                2 -> "Unsaved Contact"
                else -> "Tab One"
            }
        }.attach()

    }

    private fun requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACT_PERMISSION_CODE
            )
        }
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration(res.configuration)
        config.fontScale = 1.0f // Prevent font scaling
        createConfigurationContext(config) //Use createConfigurationContext instead
        return res
    }

}