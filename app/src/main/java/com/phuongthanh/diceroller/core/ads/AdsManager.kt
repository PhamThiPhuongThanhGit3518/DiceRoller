package com.phuongthanh.diceroller.core.ads

import android.app.Activity
import com.cyber.ads.remote.InterHolder

object AdsManager {

    fun loadNativeLanguage(activity: Activity) {
        // Preload native language ad via RemoteConfig holder if needed
    }

    fun loadAndShowInter(
        activity: Activity,
        interHolder: InterHolder?,
        onDismiss: () -> Unit
    ) {
        // Tạm thời bypass quảng cáo, gọi callback trực tiếp
        onDismiss()
    }
}
