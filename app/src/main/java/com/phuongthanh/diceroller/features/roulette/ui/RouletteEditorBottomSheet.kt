package com.phuongthanh.diceroller.features.roulette.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetRouletteEditorBinding
import com.phuongthanh.diceroller.databinding.ItemWheelEditorSectorBinding
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset
import com.phuongthanh.diceroller.features.roulette.model.WheelSector
import java.util.UUID

class RouletteEditorBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRouletteEditorBinding? = null
    private val binding get() = _binding!!

    private val availableColors = listOf(
        "#EF4444", "#F97316", "#F59E0B", "#10B981",
        "#059669", "#06B6D4", "#3B82F6", "#6366F1",
        "#8B5CF6", "#EC4899", "#14B8A6", "#64748B"
    )

    private var selectedColorHex = availableColors[0]

    var initialTitle: String = ""
    var initialSectors: List<WheelSector> = emptyList()
    var onSave: ((String, List<WheelSector>) -> Unit)? = null
    var onSectorsSaved: ((List<WheelSector>) -> Unit)? = null

    private val currentSectors = mutableListOf<WheelSector>()
    private lateinit var adapter: EditorSectorAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRouletteEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etRouletteTitle.setText(initialTitle)
        if (initialTitle.isBlank()) {
            binding.tvTitle.text = getString(R.string.roulette_create_title)
        } else {
            binding.tvTitle.text = getString(R.string.roulette_editor_title)
        }

        currentSectors.clear()
        if (initialSectors.isNotEmpty()) {
            currentSectors.addAll(initialSectors)
        } else {
            // Default options for new roulette with distinct colors
            currentSectors.addAll(
                listOf(
                    WheelSector(UUID.randomUUID().toString(), "Option 1", availableColors[0]),
                    WheelSector(UUID.randomUUID().toString(), "Option 2", availableColors[3]),
                    WheelSector(UUID.randomUUID().toString(), "Option 3", availableColors[6]),
                    WheelSector(UUID.randomUUID().toString(), "Option 4", availableColors[2])
                )
            )
        }

        selectedColorHex = availableColors[currentSectors.size % availableColors.size]
        setupColorPalette()

        adapter = EditorSectorAdapter()
        binding.rvEditorSectors.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEditorSectors.adapter = adapter

        binding.btnAddOption.setOnDebouncedClickListener {
            addNewOption()
        }

        binding.btnCancel.setOnDebouncedClickListener {
            dismiss()
        }

        binding.btnSave.setOnDebouncedClickListener {
            saveOptions()
        }
    }

    private fun setupColorPalette() {
        binding.layoutColorPalette.removeAllViews()
        val density = resources.displayMetrics.density
        val sizePx = (30 * density).toInt()
        val marginPx = (4 * density).toInt()

        availableColors.forEach { colorHex ->
            val colorView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    setMargins(marginPx, marginPx, marginPx, marginPx)
                }
                val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(colorHex))
                    if (isSelected) {
                        setStroke((3 * density).toInt(), Color.WHITE)
                    }
                }
                setOnClickListener {
                    selectedColorHex = colorHex
                    setupColorPalette()
                }
            }
            binding.layoutColorPalette.addView(colorView)
        }
    }

    private fun addNewOption() {
        val text = binding.etNewOption.text?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return

        if (currentSectors.size >= 20) {
            binding.tvEditorError.text = getString(R.string.roulette_max_options_error)
            binding.tvEditorError.visibility = View.VISIBLE
            return
        }

        val newSector = WheelSector(
            id = UUID.randomUUID().toString(),
            text = text,
            colorHex = selectedColorHex
        )
        currentSectors.add(newSector)
        adapter.notifyItemInserted(currentSectors.size - 1)
        binding.rvEditorSectors.smoothScrollToPosition(currentSectors.size - 1)
        binding.etNewOption.setText("")
        binding.tvEditorError.visibility = View.GONE

        // Auto advance selected color to next color in palette
        val nextIdx = (availableColors.indexOf(selectedColorHex) + 1) % availableColors.size
        selectedColorHex = availableColors[nextIdx]
        setupColorPalette()
    }

    private fun saveOptions() {
        if (currentSectors.size < 2) {
            binding.tvEditorError.text = getString(R.string.roulette_min_options_error)
            binding.tvEditorError.visibility = View.VISIBLE
            return
        }

        var title = binding.etRouletteTitle.text?.toString()?.trim().orEmpty()
        if (title.isBlank()) {
            title = getString(R.string.roulette_default_new_title)
        }

        onSave?.invoke(title, currentSectors.toList())
        onSectorsSaved?.invoke(currentSectors.toList())
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class EditorSectorAdapter : RecyclerView.Adapter<EditorSectorAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemWheelEditorSectorBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemBinding = ItemWheelEditorSectorBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(itemBinding)
        }

        override fun getItemCount(): Int = currentSectors.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sector = currentSectors[position]
            holder.binding.tvSectorText.text = sector.text

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                try {
                    setColor(Color.parseColor(sector.colorHex))
                } catch (_: Exception) {
                    setColor(Color.parseColor("#10B981"))
                }
            }
            holder.binding.viewColorDot.background = bg

            // Tapping color dot cycles to next color in palette
            holder.binding.viewColorDot.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION && currentPos in currentSectors.indices) {
                    val currentHex = currentSectors[currentPos].colorHex
                    val currentIdx = availableColors.indexOf(currentHex)
                    val nextHex = availableColors[(currentIdx + 1) % availableColors.size]
                    currentSectors[currentPos] = currentSectors[currentPos].copy(colorHex = nextHex)
                    notifyItemChanged(currentPos)
                }
            }

            holder.binding.btnDelete.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    if (currentSectors.size <= 2) {
                        this@RouletteEditorBottomSheet.binding.tvEditorError.text =
                            getString(R.string.roulette_min_options_error)
                        this@RouletteEditorBottomSheet.binding.tvEditorError.visibility = View.VISIBLE
                        return@setOnClickListener
                    }
                    currentSectors.removeAt(currentPos)
                    notifyItemRemoved(currentPos)
                    this@RouletteEditorBottomSheet.binding.tvEditorError.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        const val TAG = "RouletteEditorBottomSheet"

        fun newInstanceForCreate(): RouletteEditorBottomSheet {
            return RouletteEditorBottomSheet()
        }

        fun newInstanceForEdit(preset: WheelPreset): RouletteEditorBottomSheet {
            return RouletteEditorBottomSheet().apply {
                initialTitle = preset.title
                initialSectors = preset.sectors
            }
        }

        fun newInstance(sectors: List<WheelSector>): RouletteEditorBottomSheet {
            return RouletteEditorBottomSheet().apply {
                initialSectors = sectors
            }
        }
    }
}
