package com.phuongthanh.diceroller.features.roulette.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetRoulettePresetsBinding
import com.phuongthanh.diceroller.databinding.ItemWheelPresetBinding
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset

class RoulettePresetBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRoulettePresetsBinding? = null
    private val binding get() = _binding!!

    var presetsList: List<WheelPreset> = emptyList()
    var selectedPresetId: String = ""
    var onPresetSelected: ((String) -> Unit)? = null
    var onEditWheelRequested: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRoulettePresetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditWheel.setOnDebouncedClickListener {
            dismiss()
            onEditWheelRequested?.invoke()
        }

        binding.rvPresets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPresets.adapter = PresetsAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class PresetsAdapter : RecyclerView.Adapter<PresetsAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemWheelPresetBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemWheelPresetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(itemBinding)
        }

        override fun getItemCount(): Int = presetsList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val preset = presetsList[position]
            val isSelected = preset.id == selectedPresetId
            val context = holder.itemView.context

            holder.binding.tvPresetTitle.text = preset.title
            holder.binding.tvPresetCount.text = "${preset.sectors.size} options"
            holder.binding.rbSelected.isChecked = isSelected

            if (isSelected) {
                holder.binding.cardPreset.strokeColor = ContextCompat.getColor(context, R.color.accent_gold)
                holder.binding.cardPreset.strokeWidth = 4
            } else {
                holder.binding.cardPreset.strokeColor = ContextCompat.getColor(context, R.color.border_light)
                holder.binding.cardPreset.strokeWidth = 2
            }

            holder.itemView.setOnDebouncedClickListener {
                dismiss()
                onPresetSelected?.invoke(preset.id)
            }
        }
    }

    companion object {
        const val TAG = "RoulettePresetBottomSheet"

        fun newInstance(
            presets: List<WheelPreset>,
            selectedId: String
        ): RoulettePresetBottomSheet {
            return RoulettePresetBottomSheet().apply {
                presetsList = presets
                selectedPresetId = selectedId
            }
        }
    }
}
