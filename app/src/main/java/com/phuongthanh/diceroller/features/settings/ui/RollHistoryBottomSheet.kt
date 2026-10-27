package com.phuongthanh.diceroller.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetRollHistoryBinding
import com.phuongthanh.diceroller.databinding.ItemRollHistoryBinding
import com.phuongthanh.diceroller.features.settings.model.RollHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RollHistoryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRollHistoryBinding? = null
    private val binding get() = _binding!!

    var historyList: List<RollHistoryItem> = emptyList()
    var onClearHistory: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRollHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (historyList.isEmpty()) {
            binding.tvEmptyHistory.visibility = View.VISIBLE
            binding.rvRollHistory.visibility = View.GONE
        } else {
            binding.tvEmptyHistory.visibility = View.GONE
            binding.rvRollHistory.visibility = View.VISIBLE
            binding.rvRollHistory.layoutManager = LinearLayoutManager(requireContext())
            binding.rvRollHistory.adapter = HistoryAdapter(historyList)
        }

        binding.btnClearHistory.setOnDebouncedClickListener {
            onClearHistory?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class HistoryAdapter(private val list: List<RollHistoryItem>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        inner class ViewHolder(val binding: ItemRollHistoryBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemRollHistoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(itemBinding)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvHistoryScore.text = item.totalScore.toString()
            holder.binding.tvHistoryMode.text = item.mode
            holder.binding.tvHistoryDetails.text = item.details
            holder.binding.tvHistoryTime.text = timeFormat.format(Date(item.timestamp))
        }
    }

    companion object {
        const val TAG = "RollHistoryBottomSheet"

        fun newInstance(history: List<RollHistoryItem>): RollHistoryBottomSheet {
            return RollHistoryBottomSheet().apply {
                historyList = history
            }
        }
    }
}
