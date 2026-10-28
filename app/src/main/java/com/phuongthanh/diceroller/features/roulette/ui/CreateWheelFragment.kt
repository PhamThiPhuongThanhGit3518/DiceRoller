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
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentCreateWheelBinding
import com.phuongthanh.diceroller.databinding.ItemWheelOptionRowBinding
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset
import com.phuongthanh.diceroller.features.roulette.model.WheelSector
import kotlinx.coroutines.launch
import java.util.UUID

class CreateWheelFragment : BaseFragment<FragmentCreateWheelBinding>(FragmentCreateWheelBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private val sectors = mutableListOf<WheelSector>()
    private lateinit var adapter: OptionsAdapter

    override fun initViews() {
        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        adapter = OptionsAdapter(
            onEditClick = { sector, position ->
                openEditOptionSheet(sector, position)
            },
            onDeleteClick = { position ->
                sectors.removeAt(position)
                updateUiState()
            }
        )

        binding.rvOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOptions.adapter = adapter

        binding.btnAddOptionEmpty.setOnDebouncedClickListener {
            openAddOptionSheet()
        }

        binding.btnAddOptionMore.setOnDebouncedClickListener {
            if (sectors.size < 20) {
                openAddOptionSheet()
            } else {
                Toast.makeText(requireContext(), R.string.roulette_max_options_error, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDone.setOnDebouncedClickListener {
            saveNewWheel()
        }

        updateUiState()
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

        if (sectors.isEmpty()) {
            binding.layoutEmptyOptions.visibility = View.VISIBLE
            binding.btnAddOptionMore.visibility = View.GONE
        } else {
            binding.layoutEmptyOptions.visibility = View.GONE
            binding.btnAddOptionMore.visibility = if (sectors.size < 20) View.VISIBLE else View.GONE
        }

        binding.previewWheel.setSectors(sectors)

        val canSave = sectors.size >= 2
        binding.btnDone.isEnabled = canSave
        binding.btnDone.alpha = if (canSave) 1.0f else 0.5f
    }

    private fun saveNewWheel() {
        val title = binding.etWheelName.text?.toString()?.trim().orEmpty()
        val finalTitle = title.ifEmpty { getString(R.string.roulette_default_new_title) }

        if (sectors.size < 2) {
            Toast.makeText(requireContext(), R.string.roulette_min_options_error, Toast.LENGTH_SHORT).show()
            return
        }

        val newPreset = WheelPreset(
            id = UUID.randomUUID().toString(),
            title = finalTitle,
            sectors = sectors.toList()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            appContainer.rouletteRepository.savePreset(newPreset)
            val bundle = RouletteSpinFragment.newBundle(newPreset.id)
            findNavController().safeNavigate(R.id.action_createWheelFragment_to_rouletteSpinFragment, bundle)
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
