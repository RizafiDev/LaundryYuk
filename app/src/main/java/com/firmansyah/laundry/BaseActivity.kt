package com.firmansyah.laundry

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateBaseContextLocale(newBase))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val language = sharedPref.getString("language", "id") ?: "id"

        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply language on every activity creation
        applyStoredLanguage()
    }

    private fun applyStoredLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val language = sharedPref.getString("language", "id") ?: "id"
        LanguageHelper.setAppLanguage(this, language)
    }

    // Method untuk refresh bahasa tanpa restart aplikasi
    protected fun refreshLanguage() {
        recreate()
    }

    // Method untuk mendapatkan instance MainActivity jika ada
    companion object {
        var mainActivityInstance: MainActivity? = null
    }
}