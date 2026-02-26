package com.example.mislugares

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class PreferenciasFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Carga las preferencias desde el archivo XML
        setPreferencesFromResource(R.xml.preferencias, rootKey)
    }
}