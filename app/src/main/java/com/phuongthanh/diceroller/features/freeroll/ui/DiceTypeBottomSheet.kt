package com.phuongthanh.diceroller.features.freeroll.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetDiceTypeBinding
import com.phuongthanh.diceroller.databinding.ItemDiceTypeBinding
import com.phuongthanh.diceroller.core.model.DiceFaceStyle
import com.phuongthanh.diceroller.features.freeroll.model.DiceType

class DiceTypeBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDiceTypeBinding? = null
    private val binding get() = _binding!!

    var selectedType: DiceType = DiceType.D6
    var selectedFaceStyle: DiceFaceStyle = DiceFaceStyle.DOTS
    var onDiceTypeSelected: ((DiceType) -> Unit)? = null
    var onFaceStyleSelected: ((DiceFaceStyle) -> Unit)? = null
    var onCustomRangeRequested: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDiceTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = DiceType.entries
        val adapter = DiceTypeAdapter(items, selectedType) { clickedType ->
            if (clickedType == DiceType.CUSTOM) {
                dismiss()
                onCustomRangeRequested?.invoke()
            } else {
                dismiss()
                onDiceTypeSelected?.invoke(clickedType)
            }
        }

        binding.rvDiceTypes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvDiceTypes.adapter = adapter

        // Setup Face Style Toggle in Sheet
        val targetStyleId = if (selectedFaceStyle == DiceFaceStyle.NUMBERS) R.id.btnSheetStyleNumbers else R.id.btnSheetStyleDots
        binding.toggleGroupSheetStyle.check(targetStyleId)
        binding.toggleGroupSheetStyle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val style = if (checkedId == R.id.btnSheetStyleNumbers) DiceFaceStyle.NUMBERS else DiceFaceStyle.DOTS
            selectedFaceStyle = style
            onFaceStyleSelected?.invoke(style)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class DiceTypeAdapter(
        private val list: List<DiceType>,
        private val currentSelected: DiceType,
        private val onItemClick: (DiceType) -> Unit
    ) : RecyclerView.Adapter<DiceTypeAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemDiceTypeBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemDiceTypeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(itemBinding)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val context = holder.itemView.context
            holder.binding.tvDiceTypeCode.text = item.code
            holder.binding.tvDiceTypeName.text = context.getString(item.displayNameRes)

            val isSelected = item == currentSelected
            if (isSelected) {
                holder.binding.cardDiceType.strokeColor = ContextCompat.getColor(context, R.color.accent_gold)
                holder.binding.cardDiceType.strokeWidth = 4
                holder.binding.tvDiceTypeCode.setBackgroundResource(R.drawable.bg_pill_control_selected)
                holder.binding.tvDiceTypeCode.setTextColor(ContextCompat.getColor(context, R.color.black))
            } else {
                holder.binding.cardDiceType.strokeColor = ContextCompat.getColor(context, R.color.border_light)
                holder.binding.cardDiceType.strokeWidth = 2
                holder.binding.tvDiceTypeCode.setBackgroundResource(R.drawable.bg_pill_control)
                holder.binding.tvDiceTypeCode.setTextColor(ContextCompat.getColor(context, R.color.text_primary_light))
            }

            holder.itemView.setOnDebouncedClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        const val TAG = "DiceTypeBottomSheet"

        fun newInstance(currentType: DiceType, currentStyle: DiceFaceStyle = DiceFaceStyle.DOTS): DiceTypeBottomSheet {
            return DiceTypeBottomSheet().apply {
                selectedType = currentType
                selectedFaceStyle = currentStyle
            }
        }
    }
}
