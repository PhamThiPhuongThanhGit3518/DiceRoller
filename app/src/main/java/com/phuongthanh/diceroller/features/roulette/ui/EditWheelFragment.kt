package com.phuongthanh.diceroller.features.roulette.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentEditWheelBinding
import com.phuongthanh.diceroller.databinding.ItemWheelOptionRowBinding
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset
import com.phuongthanh.diceroller.features.roulette.model.WheelSector
import kotlinx.coroutines.launch
import java.util.UUID

class EditWheelFragment : BaseFragment<FragmentEditWheelBinding>(FragmentEditWheelBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private var activePresetId: String = ""
    private var currentPreset: WheelPreset? = null
    private val sectors = mutableListOf<WheelSector>()
    private lateinit var adapter: OptionsAdapter

    override fun initViews() {
        activePresetId = arguments?.getString("presetId").orEmpty()

        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        binding.btnCancel.setOnDebouncedClickListener {
            findNavController().navigateUp()
        }

        adapter = OptionsAdapter(
            onEditClick = { sector, position ->
                openEditOptionSheet(sector, position)
            },
            onDeleteClick = { position ->
                if (sectors.size > 2) {
                    sectors.removeAt(position)
                    updateUiState()
                } else {
                    Toast.makeText(requireContext(), R.string.roulette_min_options_error, Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOptions.adapter = adapter

        binding.btnMoreOptions.setOnDebouncedClickListener {
            if (sectors.size < 20) {
                openAddOptionSheet()
            } else {
                Toast.makeText(requireContext(), R.string.roulette_max_options_error, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDone.setOnDebouncedClickListener {
            saveEditedWheel()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val preset = appContainer.rouletteRepository.getPresetById(activePresetId)
            if (preset != null) {
                currentPreset = preset
                binding.etWheelName.setText(preset.title)
                sectors.clear()
                sectors.addAll(preset.sectors)
                updateUiState()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun openAddOptionSheet() {
        val sheet = AddOptionBottomSheet.newInstanceForAdd().apply {
            onSave = { text, colorHex ->
                sectors.add(WheelSector(UUID.randomUUID().toString(), text, colorHex))
                updateUiState()
            }
        }
        sheet.show(childFragmentManager, AddOptionBottomSheet.TAG)
    }

    private fun openEditOptionSheet(sector: WheelSector, position: Int) {
        val sheet = AddOptionBottomSheet.newInstanceForEdit(sector.text, sector.colorHex).apply {
            onSave = { text, colorHex ->
                sectors[position] = sector.copy(text = text, colorHex = colorHex)
                updateUiState()
            }
        }
        sheet.show(childFragmentManager, AddOptionBottomSheet.TAG)
    }

    private fun updateUiState() {
        binding.tvOptionsCounter.text = "${sectors.size}/20"
        adapter.submitList(sectors)
        binding.previewWheel.setSectors(sectors)

        val canSave = sectors.size >= 2
        binding.btnDone.isEnabled = canSave
        binding.btnDone.alpha = if (canSave) 1.0f else 0.5f
    }

    private fun saveEditedWheel() {
        val title = binding.etWheelName.text?.toString()?.trim().orEmpty()
        val finalTitle = title.ifEmpty { currentPreset?.title ?: getString(R.string.roulette_default_new_title) }

        if (sectors.size < 2) {
            Toast.makeText(requireContext(), R.string.roulette_min_options_error, Toast.LENGTH_SHORT).show()
            return
        }

        val updated = (currentPreset ?: WheelPreset(
            id = activePresetId,
            title = finalTitle,
            sectors = sectors
        )).copy(
            title = finalTitle,
            sectors = sectors.toList()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            appContainer.rouletteRepository.savePreset(updated)
            findNavController().navigateUp()
        }
    }

    private class OptionsAdapter(
        private val onEditClick: (WheelSector, Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {

        private val items = mutableListOf<WheelSector>()

        fun submitList(newItems: List<WheelSector>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemWheelOptionRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sector = items[position]
            holder.binding.tvOptionText.text = sector.text

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(sector.colorHex))
            }
            holder.binding.viewSectorColor.background = bg

            holder.binding.btnEditOption.setOnClickListener {
                onEditClick(sector, position)
            }

            holder.binding.btnDeleteOption.setOnClickListener {
                onDeleteClick(position)
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemWheelOptionRowBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
