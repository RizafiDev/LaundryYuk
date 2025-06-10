package com.firmansyah.laundry

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import java.util.*

object LanguageHelper {

    fun setAppLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        // For API 25+ we need to use createConfigurationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(config)
        }

        // Update configuration for all API levels
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Alternative method for activities that handles context wrapping properly
    fun wrapContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    fun getCurrentLanguage(context: Context): String {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPref.getString("language", "id") ?: "id"
    }

    fun saveLanguagePreference(context: Context, language: String) {
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language", language)
            apply()
        }
    }

    fun applyLanguageToActivity(activity: AppCompatActivity) {
        val currentLanguage = getCurrentLanguage(activity)
        setAppLanguage(activity, currentLanguage)
    }
}