package com.example.mislugares

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mislugares.databinding.ActivityPreferenciasBinding

class PreferenciasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferenciasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarPreferencias)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_preferencias, PreferenciasFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}