package com.phuongthanh.diceroller.features.settings.ui

import android.graphics.Paint
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentPremiumBinding

enum class PremiumPlan {
    FREE,
    WEEKLY,
    YEARLY
}

class PremiumFragment : BaseFragment<FragmentPremiumBinding>(FragmentPremiumBinding::inflate) {

    private var selectedPlan: PremiumPlan = PremiumPlan.FREE

    private val billingManager by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer.billingManager
    }

    override fun initViews() {
        // Back / Close button
        binding.btnClose.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        // Strikethrough for yearly original price
        binding.tvYearlyOriginalPrice.paintFlags =
            binding.tvYearlyOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Plan Selection listeners
        setupPlanSelection()

        // CTA Purchase / Unlock
        binding.btnUnlockPremium.setOnDebouncedClickListener {
            billingManager.purchaseRemoveAds(requireActivity()) { success, _ ->
                if (success) {
                    context?.let { ctx ->
                        Toast.makeText(ctx, R.string.premium_success_toast, Toast.LENGTH_SHORT).show()
                    }
                    findNavController().navigateUp()
                }
            }
        }

        // Restore Purchases
        binding.btnRestorePurchases.setOnDebouncedClickListener {
            billingManager.restorePurchases { isVip, _ ->
                val ctx = context ?: return@restorePurchases
                if (isVip) {
                    Toast.makeText(ctx, R.string.premium_restore_success, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(ctx, R.string.premium_restore_empty, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun initObservers() {
        observeVipStatus()
    }

    private fun setupPlanSelection() {
        selectPlan(PremiumPlan.FREE)

        binding.cardPlanFree.setOnDebouncedClickListener {
            selectPlan(PremiumPlan.FREE)
        }

        binding.cardPlanWeekly.setOnDebouncedClickListener {
            selectPlan(PremiumPlan.WEEKLY)
        }

        binding.cardPlanYearly.setOnDebouncedClickListener {
            selectPlan(PremiumPlan.YEARLY)
        }
    }

    private fun selectPlan(plan: PremiumPlan) {
        selectedPlan = plan

        // Plan 1: Free Trial
        val isFree = plan == PremiumPlan.FREE
        binding.cardPlanFree.setBackgroundResource(
            if (isFree) R.drawable.bg_plan_card_selected else R.drawable.bg_plan_card_unselected
        )
        binding.radioFree.setImageResource(
            if (isFree) R.drawable.ic_plan_radio_checked else R.drawable.ic_plan_radio_unchecked
        )

        // Plan 2: Weekly
        val isWeekly = plan == PremiumPlan.WEEKLY
        binding.cardPlanWeekly.setBackgroundResource(
            if (isWeekly) R.drawable.bg_plan_card_selected else R.drawable.bg_plan_card_unselected
        )
        binding.radioWeekly.setImageResource(
            if (isWeekly) R.drawable.ic_plan_radio_checked else R.drawable.ic_plan_radio_unchecked
        )

        // Plan 3: Yearly
        val isYearly = plan == PremiumPlan.YEARLY
        binding.cardPlanYearly.setBackgroundResource(
            if (isYearly) R.drawable.bg_plan_card_selected else R.drawable.bg_plan_card_unselected
        )
        binding.radioYearly.setImageResource(
            if (isYearly) R.drawable.ic_plan_radio_checked else R.drawable.ic_plan_radio_unchecked
        )
    }

    private fun observeVipStatus() {
        billingManager.isVipFlow.collectWithLifecycle(viewLifecycleOwner) { isVip ->
            if (isVip) {
                binding.layoutPlans.visibility = View.GONE
                binding.btnUnlockPremium.visibility = View.GONE
                binding.layoutVipActive.visibility = View.VISIBLE
            } else {
                binding.layoutPlans.visibility = View.VISIBLE
                binding.btnUnlockPremium.visibility = View.VISIBLE
                binding.layoutVipActive.visibility = View.GONE
            }
        }
    }
}
