package com.phuongthanh.diceroller.features.roulette.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.DialogRouletteResultBinding

class RouletteResultDialog : DialogFragment() {

    private var _binding: DialogRouletteResultBinding? = null
    private val binding get() = _binding!!

    private var winnerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        winnerName = arguments?.getString(ARG_WINNER).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRouletteResultBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvWinnerName.text = winnerName

        binding.btnOk.setOnDebouncedClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RouletteResultDialog"
        private const val ARG_WINNER = "arg_winner"

        fun newInstance(winner: String): RouletteResultDialog {
            return RouletteResultDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_WINNER, winner)
                }
            }
        }
    }
}
