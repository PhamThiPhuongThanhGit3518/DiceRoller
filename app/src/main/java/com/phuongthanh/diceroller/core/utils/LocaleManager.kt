package com.phuongthanh.diceroller.core.utils

import android.content.Context
import com.cyber.ads.onboading.BaseLanguageActivity
import java.util.Locale

object LocaleManager {

    fun getCurrentLanguage(context: Context): String {
        return try {
            BaseLanguageActivity.currentLanguage(context) ?: "en"
        } catch (_: Exception) {
            "en"
        }
    }

    fun setLocale(context: Context) {
        val language = getCurrentLanguage(context)
        val myLocale = Locale.forLanguageTag(language)
        Locale.setDefault(myLocale)
        val resource = context.resources
        val displayMetrics = resource.displayMetrics
        val configuration = resource.configuration
        configuration.setLocale(myLocale)
        @Suppress("DEPRECATION")
        resource.updateConfiguration(configuration, displayMetrics)
    }
}
