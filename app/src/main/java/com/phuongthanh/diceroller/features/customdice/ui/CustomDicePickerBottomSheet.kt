package com.phuongthanh.diceroller.features.customdice.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetCustomDicePickerBinding
import com.phuongthanh.diceroller.databinding.ItemCustomDiceSetBinding
import com.phuongthanh.diceroller.features.customdice.model.CustomDice

class CustomDicePickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomDicePickerBinding? = null
    private val binding get() = _binding!!

    var diceList: List<CustomDice> = emptyList()
    var selectedDiceId: String = ""
    var onDiceSelected: ((String) -> Unit)? = null
    var onEditDice: ((CustomDice) -> Unit)? = null
    var onDeleteDice: ((String) -> Unit)? = null
    var onResetPresets: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomDicePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCustomDiceSets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomDiceSets.adapter = DiceSetAdapter()

        binding.btnResetPresets.setOnDebouncedClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_reset_title)
                .setMessage(R.string.confirm_reset_presets_desc)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_reset_default) { _, _ ->
                    onResetPresets?.invoke()
                    dismiss()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class DiceSetAdapter : RecyclerView.Adapter<DiceSetAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemCustomDiceSetBinding) : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemCustomDiceSetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(itemBinding)
        }

        override fun getItemCount(): Int = diceList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = diceList[position]
            val isSelected = item.id == selectedDiceId
            val context = holder.itemView.context

            holder.itemBinding.tvDiceTitle.text = item.title
            holder.itemBinding.tvFacesSummary.text = "${item.faces.size} faces: " + item.faces.joinToString(", ") { it.text }

            if (isSelected) {
                holder.itemBinding.cardDiceSet.strokeColor = ContextCompat.getColor(context, R.color.accent_gold)
                holder.itemBinding.cardDiceSet.strokeWidth = 4
            } else {
                holder.itemBinding.cardDiceSet.strokeColor = ContextCompat.getColor(context, R.color.border_light)
                holder.itemBinding.cardDiceSet.strokeWidth = 2
            }

            // Edit button (works for all custom dice, including presets)
            holder.itemBinding.btnEditDice.setOnClickListener {
                dismiss()
                onEditDice?.invoke(item)
            }

            // Delete button (works for all custom dice, including presets)
            holder.itemBinding.btnDeleteDice.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle(R.string.delete_confirm_title)
                    .setMessage(R.string.delete_confirm_desc)
                    .setPositiveButton(R.string.btn_delete) { _, _ ->
                        onDeleteDice?.invoke(item.id)
                        dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }

            holder.itemView.setOnDebouncedClickListener {
                dismiss()
                onDiceSelected?.invoke(item.id)
            }
        }
    }

    companion object {
        const val TAG = "CustomDicePickerBottomSheet"

        fun newInstance(list: List<CustomDice>, selectedId: String): CustomDicePickerBottomSheet {
            return CustomDicePickerBottomSheet().apply {
                diceList = list
                selectedDiceId = selectedId
            }
        }
    }
}
