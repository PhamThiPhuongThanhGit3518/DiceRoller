package com.phuongthanh.diceroller.features.settings.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.LocaleManager
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setFigmaSettingsBannerBackground
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentSettingsBinding
import com.phuongthanh.diceroller.features.language.LanguageActivity
import java.util.Locale

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override fun initViews() {
        setupCardBackgrounds()
        setupClickListeners()
        updateLanguageDisplay()
    }

    private fun setupCardBackgrounds() {
        binding.bannerPremium.setFigmaSettingsBannerBackground()
    }

    override fun onResume() {
        super.onResume()
        updateLanguageDisplay()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        binding.bannerPremium.setOnDebouncedClickListener {
            openPremiumPaywall()
        }

        binding.btnUpgrade.setOnDebouncedClickListener {
            openPremiumPaywall()
        }

        binding.cardLanguage.setOnDebouncedClickListener {
            openLanguageSelection()
        }

        binding.cardShare.setOnDebouncedClickListener {
            shareApp()
        }

        binding.cardFeedback.setOnDebouncedClickListener {
            rateApp()
        }

        binding.cardPrivacyPolicy.setOnDebouncedClickListener {
            openPrivacyPolicy()
        }

        binding.btnNext.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }
    }

    private fun openPremiumPaywall() {
        findNavController().safeNavigate(R.id.action_settingsFragment_to_premiumFragment)
    }

    private fun updateLanguageDisplay() {
        val currentLangCode = LocaleManager.getCurrentLanguage(requireContext())
        val langName = when (currentLangCode.lowercase()) {
            "vi" -> "Tiếng Việt"
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "zh" -> "中文"
            "pt" -> "Português"
            "hi" -> "हिन्दी"
            else -> Locale.forLanguageTag(currentLangCode).getDisplayLanguage(Locale.getDefault()).ifBlank { currentLangCode }
        }
        binding.tvLanguageSubtitle.apply {
            text = langName
            isVisible = langName.isNotBlank()
        }
    }

    private fun openLanguageSelection() {
        val intent = Intent(requireContext(), LanguageActivity::class.java).apply {
            putExtra("fromSplash", false)
        }
        startActivity(intent)
    }

    private fun shareApp() {
        val packageName = requireContext().packageName
        val shareContent = getString(R.string.settings_share_content, packageName)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareContent)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.settings_item_share))
        startActivity(shareIntent)
    }

    private fun rateApp() {
        val packageName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun openPrivacyPolicy() {
        val privacyUrl = "https://policies.google.com/privacy"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
        } catch (_: Exception) {
            // Fallback gracefully if no browser found
        }
    }
}
