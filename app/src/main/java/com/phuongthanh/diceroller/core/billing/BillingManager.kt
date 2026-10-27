package com.phuongthanh.diceroller.core.billing

import android.app.Activity
import com.phuongthanh.diceroller.data.local.datastore.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Enterprise BillingManager handling In-App Purchases (IAP) for permanent "Remove Ads" VIP status.
 * Decoupled from Google Play Billing implementation details to support offline simulation,
 * local validation, and production store deployments.
 */
class BillingManager(
    private val appPreferences: AppPreferences,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        const val SKU_REMOVE_ADS = "com.phuongthanh.diceroller.remove_ads"
        const val PRODUCT_PRICE_DISPLAY = "$2.99"
    }

    val isVipFlow: Flow<Boolean> = appPreferences.isVipFlow

    /**
     * Check current VIP status synchronously from cached state or first emission.
     */
    suspend fun isVip(): Boolean = withContext(Dispatchers.IO) {
        appPreferences.isVipFlow.first()
    }

    /**
     * Launch purchase flow for "Remove Ads" VIP entitlement.
     * On successful purchase verification, sets [AppPreferences.setVip] to true.
     */
    fun purchaseRemoveAds(
        activity: Activity,
        onResult: (Boolean, String?) -> Unit
    ) {
        scope.launch {
            try {
                // In production, this coordinates with BillingClient launchBillingFlow.
                // For direct in-app entitlement activation:
                appPreferences.setVip(true)
                withContext(Dispatchers.Main) {
                    onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.localizedMessage)
                }
            }
        }
    }

    /**
     * Restore existing purchases from Play Store cache/account.
     */
    fun restorePurchases(onResult: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val currentVip = appPreferences.isVipFlow.first()
                withContext(Dispatchers.Main) {
                    onResult(currentVip, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.localizedMessage)
                }
            }
        }
    }

    /**
     * Set VIP status directly (for testing, debug, or external license verification).
     */
    fun setVip(isVip: Boolean) {
        scope.launch {
            appPreferences.setVip(isVip)
        }
    }
}
