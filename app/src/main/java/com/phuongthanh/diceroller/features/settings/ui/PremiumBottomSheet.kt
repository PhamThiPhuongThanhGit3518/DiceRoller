package com.phuongthanh.diceroller.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.billing.BillingManager
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetPremiumBinding

class PremiumBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPremiumBinding? = null
    private val binding get() = _binding!!

    private val billingManager by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer.billingManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnDebouncedClickListener {
            dismiss()
        }

        binding.btnUnlockPremium.text = getString(
            R.string.premium_btn_buy,
            BillingManager.PRODUCT_PRICE_DISPLAY
        )

        binding.btnUnlockPremium.setOnDebouncedClickListener {
            billingManager.purchaseRemoveAds(requireActivity()) { success, _ ->
                if (success) {
                    context?.let { ctx ->
                        Toast.makeText(ctx, R.string.premium_success_toast, Toast.LENGTH_SHORT).show()
                    }
                    dismiss()
                }
            }
        }

        binding.btnRestorePurchases.setOnDebouncedClickListener {
            billingManager.restorePurchases { isVip, _ ->
                val ctx = context ?: return@restorePurchases
                if (isVip) {
                    Toast.makeText(ctx, R.string.premium_restore_success, Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(ctx, R.string.premium_restore_empty, Toast.LENGTH_SHORT).show()
                }
            }
        }

        observeVipStatus()
    }

    private fun observeVipStatus() {
        billingManager.isVipFlow.collectWithLifecycle(this) { isVip ->
            if (isVip) {
                binding.layoutPurchaseButtons.visibility = View.GONE
                binding.layoutVipActive.visibility = View.VISIBLE
            } else {
                binding.layoutPurchaseButtons.visibility = View.VISIBLE
                binding.layoutVipActive.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PremiumBottomSheet"

        fun newInstance(): PremiumBottomSheet = PremiumBottomSheet()
    }
}
