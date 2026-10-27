package com.phuongthanh.diceroller.features.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.MainActivity
import com.phuongthanh.diceroller.core.ads.AdsManager
import com.phuongthanh.diceroller.core.ads.RemoteConfig
import com.phuongthanh.diceroller.core.base.BaseActivity
import com.phuongthanh.diceroller.databinding.ActivitySplashBinding
import com.phuongthanh.diceroller.features.intro.IntroActivity
// import com.phuongthanh.diceroller.features.language.LanguageActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    override fun initViews() {
        // AdsManager.loadNativeLanguage(this) // Tạm thời tắt khi chưa có Firebase/Ads

        lifecycleScope.launch {
            delay(1200L) // Hiển thị Splash ngắn
            val appPreferences = (application as DiceRollerApplication).appContainer.appPreferences
            val isFirstOpen = appPreferences.isFirstOpenFlow.first()

            /*
            // Tạm thời comment luồng LanguageActivity do chưa tích hợp Firebase Remote Config
            val shouldShowLanguageIntro = isFirstOpen || RemoteConfig.showLanguageIntroEveryOpen()
            if (shouldShowLanguageIntro) {
                val intent = Intent(this@SplashActivity, LanguageActivity::class.java).apply {
                    putExtra("fromSplash", true)
                }
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
            */

            // Điều hướng trực tiếp: Lần đầu mở app vào IntroActivity, các lần sau vào MainActivity
            val targetClass = if (isFirstOpen) IntroActivity::class.java else MainActivity::class.java
            val intent = Intent(this@SplashActivity, targetClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
    }
}
