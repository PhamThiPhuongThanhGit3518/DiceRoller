package com.phuongthanh.diceroller.features.customdice.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phuongthanh.diceroller.DiceRollerApplication
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.base.BaseFragment
import com.phuongthanh.diceroller.core.utils.safeNavigate
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.FragmentCustomRollHomeBinding
import com.phuongthanh.diceroller.databinding.ItemCustomDiceSetBinding
import com.phuongthanh.diceroller.features.customdice.model.CustomDice
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomRollHomeFragment : BaseFragment<FragmentCustomRollHomeBinding>(FragmentCustomRollHomeBinding::inflate) {

    private val appContainer by lazy {
        (requireActivity().application as DiceRollerApplication).appContainer
    }

    private lateinit var adapter: CustomRollAdapter

    override fun initViews() {
        val titleHtml = "<font color='#00CAFB'>${getString(R.string.custom_roll_header_creat)}</font><font color='#FFFFFF'>${getString(R.string.custom_roll_header_suffix)}</font>"
        binding.tvHeaderTitle.text = android.text.Html.fromHtml(titleHtml, android.text.Html.FROM_HTML_MODE_LEGACY)

        binding.topBar.setOnBackClickListener {
            findNavController().navigateUp()
        }

        binding.btnCreateNewGame.setOnDebouncedClickListener {
            findNavController().safeNavigate(R.id.action_customRollHomeFragment_to_customDiceStep1Fragment)
        }

        adapter = CustomRollAdapter(
            onItemClick = { dice ->
                findNavController().safeNavigate(
                    R.id.action_customRollHomeFragment_to_customRollPlayFragment,
                    Bundle().apply { putString("diceId", dice.id) }
                )
            },
            onDeleteClick = { dice ->
                viewLifecycleOwner.lifecycleScope.launch {
                    appContainer.customDiceRepository.deleteCustomDice(dice.id)
                }
            }
        )

        binding.rvRecentGames.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentGames.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            appContainer.customDiceRepository.customDiceListFlow.collectLatest { list ->
                adapter.submitList(list)
                if (list.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvRecentGames.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvRecentGames.visibility = View.VISIBLE
                }
            }
        }
    }

    private class CustomRollAdapter(
        private val onItemClick: (CustomDice) -> Unit,
        private val onDeleteClick: (CustomDice) -> Unit
    ) : RecyclerView.Adapter<CustomRollAdapter.ViewHolder>() {

        private val items = mutableListOf<CustomDice>()

        fun submitList(newItems: List<CustomDice>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCustomDiceSetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val binding: ItemCustomDiceSetBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(dice: CustomDice) {
                binding.tvDiceTitle.text = dice.title
                val facesText = dice.faces.joinToString(", ") { it.text }
                binding.tvFacesSummary.text = facesText.ifBlank { "Chưa có nội dung mặt" }

                binding.root.setOnClickListener { onItemClick(dice) }
                binding.btnDeleteDice.setOnClickListener { onDeleteClick(dice) }
                binding.btnEditDice.visibility = View.GONE
            }
        }
    }
}
