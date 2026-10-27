package com.phuongthanh.diceroller.features.party.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetPartyQuestionBinding
import com.phuongthanh.diceroller.features.party.model.ChallengeType
import com.phuongthanh.diceroller.features.party.model.PartyChallenge

class PartyQuestionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPartyQuestionBinding? = null
    private val binding get() = _binding!!

    var packName: String = ""
    var challenge: PartyChallenge? = null
    var onRollAgain: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPartyQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPackName.text = packName.uppercase()

        val c = challenge ?: return
        binding.tvQuestionBadge.setText(c.type.titleRes)

        val badgeBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            try {
                setColor(Color.parseColor(c.type.colorHex))
            } catch (_: Exception) {
                setColor(Color.parseColor("#10B981"))
            }
        }
        binding.tvQuestionBadge.background = badgeBg
        binding.tvQuestionBadge.setTextColor(Color.WHITE)

        binding.tvQuestionText.text = c.text

        binding.btnRollAgain.setOnDebouncedClickListener {
            dismiss()
            onRollAgain?.invoke()
        }

        binding.btnDone.setOnDebouncedClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PartyQuestionBottomSheet"

        fun newInstance(
            packName: String,
            challenge: PartyChallenge,
            onRollAgain: (() -> Unit)? = null
        ): PartyQuestionBottomSheet {
            return PartyQuestionBottomSheet().apply {
                this.packName = packName
                this.challenge = challenge
                this.onRollAgain = onRollAgain
            }
        }
    }
}
