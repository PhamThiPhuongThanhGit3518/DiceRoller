package com.phuongthanh.diceroller.features.roulette.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetAddOptionBinding
import com.phuongthanh.diceroller.databinding.ItemColorSwatchBinding

class AddOptionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddOptionBinding? = null
    private val binding get() = _binding!!

    private var initialText: String = ""
    private var selectedColor: String = "#3B82F6"
    private var isEditMode: Boolean = false

    var onSave: ((text: String, colorHex: String) -> Unit)? = null

    private val availableColors = listOf(
        "#1D2939", "#344054", "#475467", "#667085", "#98A2B3", "#CBD5E1",
        "#EF4444", "#F59E0B", "#10B981", "#3B82F6", "#8B5CF6", "#EC4899"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialText = arguments?.getString(ARG_TEXT).orEmpty()
        selectedColor = arguments?.getString(ARG_COLOR) ?: "#3B82F6"
        isEditMode = arguments?.getBoolean(ARG_IS_EDIT) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSheetTitle.text = if (isEditMode) {
            getString(R.string.roulette_sheet_edit_option_title)
        } else {
            getString(R.string.roulette_sheet_add_option_title)
        }

        binding.etOptionText.setText(initialText)
        if (initialText.isNotEmpty()) {
            binding.etOptionText.setSelection(initialText.length)
        }

        val adapter = ColorSwatchAdapter(availableColors, selectedColor) { color ->
            selectedColor = color
        }

        binding.rvColorSwatches.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvColorSwatches.adapter = adapter

        binding.btnDone.setOnDebouncedClickListener {
            val text = binding.etOptionText.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), R.string.roulette_enter_option, Toast.LENGTH_SHORT).show()
                return@setOnDebouncedClickListener
            }

            onSave?.invoke(text, selectedColor)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ColorSwatchAdapter(
        private val colors: List<String>,
        private var selectedColor: String,
        private val onSelect: (String) -> Unit
    ) : RecyclerView.Adapter<ColorSwatchAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemColorSwatchBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemColorSwatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val colorHex = colors[position]
            val isSelected = colorHex.equals(selectedColor, ignoreCase = true)

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(colorHex))
            }
            holder.binding.viewColorCircle.background = bg

            holder.binding.viewSelectionRing.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

            holder.binding.root.setOnClickListener {
                selectedColor = colorHex
                notifyDataSetChanged()
                onSelect(colorHex)
            }
        }

        override fun getItemCount(): Int = colors.size
    }

    companion object {
        const val TAG = "AddOptionBottomSheet"
        private const val ARG_TEXT = "arg_text"
        private const val ARG_COLOR = "arg_color"
        private const val ARG_IS_EDIT = "arg_is_edit"

        fun newInstanceForAdd(defaultColor: String = "#3B82F6"): AddOptionBottomSheet {
            return AddOptionBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TEXT, "")
                    putString(ARG_COLOR, defaultColor)
                    putBoolean(ARG_IS_EDIT, false)
                }
            }
        }

        fun newInstanceForEdit(text: String, colorHex: String): AddOptionBottomSheet {
            return AddOptionBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TEXT, text)
                    putString(ARG_COLOR, colorHex)
                    putBoolean(ARG_IS_EDIT, true)
                }
            }
        }
    }
}
