package com.phuongthanh.diceroller

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.phuongthanh.diceroller.core.base.BaseActivity
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun initViews() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        navController?.let {
            binding.customBottomNav.setupWithNavController(it)
        }

        setupAdsAndBilling()
    }

    private fun setupAdsAndBilling() {
        // Tạm thời comment toàn bộ quảng cáo, chỉ chạy core app
        binding.bannerAdContainer.visibility = android.view.View.GONE
        binding.bannerAdContainer.removeAllViews()

        /*
        val app = application as DiceRollerApplication
        val billingManager = app.appContainer.billingManager

        billingManager.isVipFlow.collectWithLifecycle(this) { isVip ->
            if (isVip) {
                binding.bannerAdContainer.visibility = android.view.View.GONE
                binding.bannerAdContainer.removeAllViews()
            } else {
                com.phuongthanh.diceroller.core.admob.AdmobManager.loadBanner(
                    this@MainActivity,
                    binding.bannerAdContainer,
                    isVip = false
                )
                com.phuongthanh.diceroller.core.admob.AdmobManager.preloadInterstitial(
                    applicationContext,
                    isVip = false
                )
            }
        }
        */
    }
}