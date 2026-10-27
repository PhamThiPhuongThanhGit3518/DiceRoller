package com.phuongthanh.diceroller

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.phuongthanh.diceroller.core.di.AppContainer
import com.phuongthanh.diceroller.core.utils.LocaleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DiceRollerApplication : Application(), Application.ActivityLifecycleCallbacks {

    val appContainer by lazy { AppContainer(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        // Chỉ sử dụng 1 theme sáng duy nhất cho toàn bộ ứng dụng
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initFirebaseIfAvailable()
        registerActivityLifecycleCallbacks(this)

        appScope.launch {
            appContainer.appPreferences.soundEnabledFlow.collect {
                appContainer.soundManager.setMuted(!it)
            }
        }
        appScope.launch {
            appContainer.appPreferences.hapticEnabledFlow.collect {
                appContainer.hapticManager.setEnabled(it)
            }
        }
        appScope.launch {
            appContainer.appPreferences.shakeSensitivityFlow.collect {
                appContainer.shakeDetector.setSensitivityLevel(it)
            }
        }
    }

    private fun initFirebaseIfAvailable() {
        try {
            val firebaseAppClass = Class.forName("com.google.firebase.FirebaseApp")
            val getAppsMethod = firebaseAppClass.getMethod("getApps", android.content.Context::class.java)
            val apps = getAppsMethod.invoke(null, this) as? List<*>
            if (apps.isNullOrEmpty()) {
                val initializeMethod = firebaseAppClass.getMethod("initializeApp", android.content.Context::class.java)
                initializeMethod.invoke(null, this)
            }
        } catch (_: Throwable) {
            // Firebase already initialized by FirebaseInitProvider or not present
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        hideSystemBars(activity)
        LocaleManager.setLocale(activity)
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        hideSystemBars(activity)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        fun hideSystemBars(activity: Activity) {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            val window = activity.window ?: return
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
