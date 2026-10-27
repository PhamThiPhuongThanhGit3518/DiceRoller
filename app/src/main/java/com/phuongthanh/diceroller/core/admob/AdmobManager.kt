package com.phuongthanh.diceroller.core.admob

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Enterprise AdMob management with 60-second Interstitial frequency capping,
 * App Open Ad lifecycle management, Adaptive Banner embedding, and strict VIP suppression.
 */
object AdmobManager {

    // Standard Google AdMob Test Ad Unit IDs
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257390721"
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    // 60 seconds minimum interval between Interstitial ads to maintain great UX
    private const val INTERSTITIAL_CAPPING_MS = 60_000L
    private var lastInterstitialShowTime = 0L

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenLoadTime = 0L
    var isShowingAppOpenAd = false
        private set

    private var isInitialized = false

    fun initialize(context: Context) {
        // Tạm thời tắt quảng cáo
        /*
        if (isInitialized) return
        MobileAds.initialize(context) {
            isInitialized = true
        }
        */
    }

    /**
     * Preloads an Interstitial ad in the background.
     */
    fun preloadInterstitial(
        context: Context,
        adUnitId: String = TEST_INTERSTITIAL_ID,
        isVip: Boolean = false
    ) {
        // Tạm thời tắt quảng cáo
    }

    /**
     * Displays Interstitial ad with 60-second frequency capping and VIP suppression.
     * Always invokes [onDismiss] whether ad was shown, failed, or skipped due to capping/VIP.
     */
    fun showInterstitial(
        activity: Activity,
        isVip: Boolean = false,
        adUnitId: String = TEST_INTERSTITIAL_ID,
        onDismiss: () -> Unit
    ) {
        // Tạm thời tắt quảng cáo, gọi callback trực tiếp
        onDismiss()
    }

    /**
     * Loads a Banner Ad into [container]. If [isVip] is true, [container] is hidden.
     */
    fun loadBanner(
        activity: Activity,
        container: ViewGroup,
        adUnitId: String = TEST_BANNER_ID,
        isVip: Boolean = false
    ) {
        // Tạm thời tắt quảng cáo
        container.visibility = View.GONE
        container.removeAllViews()
    }

    /**
     * Preloads an App Open Ad.
     */
    fun preloadAppOpenAd(
        context: Context,
        adUnitId: String = TEST_APP_OPEN_ID,
        isVip: Boolean = false
    ) {
        // Tạm thời tắt quảng cáo
    }

    private fun isAppOpenAdAvailable(): Boolean {
        // App open ads expire after 4 hours
        return appOpenAd != null && (System.currentTimeMillis() - appOpenLoadTime < 4 * 3600_000L)
    }

    /**
     * Shows the App Open Ad if available and user is not VIP.
     */
    fun showAppOpenAd(
        activity: Activity,
        isVip: Boolean = false,
        onComplete: () -> Unit
    ) {
        // Tạm thời tắt quảng cáo, gọi callback trực tiếp
        onComplete()
    }
}
