package com.phuongthanh.diceroller.features.language

import android.content.Intent
import com.cyber.ads.onboading.BaseLanguageActivity
import com.cyber.ads.remote.NativeHolder
import com.cyber.ads.remote.NativeMultiHolder
import com.phuongthanh.diceroller.MainActivity
import com.phuongthanh.diceroller.core.ads.AdsManager
import com.phuongthanh.diceroller.core.ads.RemoteConfig

class LanguageActivity : BaseLanguageActivity() {

    // 1. Khai báo các holder quảng cáo chân trang
    override var nativeLanguage: NativeMultiHolder = RemoteConfig.NATIVE_LANGUAGE
    override var nativeLanguageRe: NativeMultiHolder? = RemoteConfig.NATIVE_LANGUAGE_RE
    override var nativeSmall: NativeHolder? = RemoteConfig.NATIVE_LANGUAGE_SMALL

    // 2. Preload trước các Native Ads cho màn Intro kế tiếp
    override var nativeIntroFullMulti: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO_FULL
    override var nativeIntroFullMultiRe: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO_FULL_RE
    override var nativeIntro: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO
    override var nativeIntroRe: NativeMultiHolder? = RemoteConfig.NATIVE_INTRO_RE

    // 3. Callback điều hướng khi bấm nút Next / Tick hoàn tất chọn ngôn ngữ
    override fun nextActivity() {
        AdsManager.loadAndShowInter(this, RemoteConfig.INTER_LANGUAGE) {
            if (fromSplash) {
                // Đi tiếp tới luồng Onboarding (Lần đầu mở app)
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("open_onboarding", true)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            } else {
                // Đổi ngôn ngữ từ màn Settings -> Reset task và quay lại MainActivity
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("open_home", true)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
