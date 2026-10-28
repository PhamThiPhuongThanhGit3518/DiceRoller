package com.phuongthanh.diceroller.features.roulette.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.collectWithLifecycle
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentRouletteBinding
import com.phuongthanh.diceroller.databinding.ItemRouletteCardGridBinding
import com.phuongthanh.diceroller.features.roulette.model.WheelPreset
import com.phuongthanh.diceroller.features.roulette.viewmodel.RouletteViewModel

class RouletteFragment : BaseFragment<FragmentRouletteBinding>(FragmentRouletteBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private val viewModel: RouletteViewModel by activityViewModels {
        RouletteViewModel.Factory(appContainer.rouletteRepository)
    }

    private lateinit var adapter: RouletteGridAdapter

    override fun initViews() {
        binding.topBar.containerAction1.setBackgroundResource(R.drawable.bg_squircle_blue_btn)
        binding.topBar.btnAction1.setColorFilter(android.graphics.Color.parseColor("#FFD700"))

        binding.topBar.setRightAction1 {
            findNavController().safeNavigate(R.id.settingsFragment)
        }

        binding.topBar.setRightAction2 {
            findNavController().safeNavigate(R.id.settingsFragment)
        }

        binding.btnAddRoulette.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_rouletteFragment_to_createWheelFragment)
        }

        binding.cardAddRoulette.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_rouletteFragment_to_createWheelFragment)
        }

        adapter = RouletteGridAdapter(
            onItemClick = { preset ->
                navigateToSpin(preset.id)
            },
            onEditClick = { preset ->
                val bundle = android.os.Bundle().apply { putString("presetId", preset.id) }
                findNavController().safeNavigate(R.id.action_rouletteFragment_to_editWheelFragment, bundle)
            }
        )

        binding.rvRoulettes.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvRoulettes.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.collectWithLifecycle(viewLifecycleOwner) { state ->
            adapter.submitList(state.presets)
        }
    }

    private fun navigateToSpin(presetId: String) {
        val bundle = RouletteSpinFragment.newBundle(presetId)
        findNavController().safeNavigate(R.id.action_rouletteFragment_to_rouletteSpinFragment, bundle)
    }

    private class RouletteGridAdapter(
        private val onItemClick: (WheelPreset) -> Unit,
        private val onEditClick: (WheelPreset) -> Unit
    ) : RecyclerView.Adapter<RouletteGridAdapter.ViewHolder>() {

        private val items = mutableListOf<WheelPreset>()

        fun submitList(newItems: List<WheelPreset>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemRouletteCardGridBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val preset = items[position]
            holder.binding.tvTitle.text = preset.title
            holder.binding.tvOptionCount.text = holder.itemView.context.getString(
                R.string.roulette_options_format, preset.sectors.size
            )

            holder.binding.btnCardAction.setOnClickListener { onEditClick(preset) }
            holder.binding.root.setOnClickListener { onItemClick(preset) }
            holder.binding.btnSpin.setOnClickListener { onItemClick(preset) }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemRouletteCardGridBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
