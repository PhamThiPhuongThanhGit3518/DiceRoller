package com.phuongthanh.diceroller.features.party.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.ItemPartyRecentBinding
import com.phuongthanh.diceroller.databinding.ItemPartyRecommendedBinding
import com.phuongthanh.diceroller.features.party.model.PartyChallengePack

class PartyRecentAdapter(
    private val onItemClick: (PartyChallengePack) -> Unit
) : RecyclerView.Adapter<PartyRecentAdapter.ViewHolder>() {

    private var items: List<PartyChallengePack> = emptyList()

    fun submitList(newItems: List<PartyChallengePack>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemPartyRecentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartyRecentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvRecentTitle.setText(item.nameRes)
        holder.binding.tvRecentSubtitle.setText(item.descRes)
        holder.binding.ivRecentIcon.setImageResource(item.iconRes)

        holder.itemView.setOnDebouncedClickListener {
            onItemClick(item)
        }
    }
}

class PartyRecommendedAdapter(
    private val onItemClick: (PartyChallengePack) -> Unit
) : RecyclerView.Adapter<PartyRecommendedAdapter.ViewHolder>() {

    private var items: List<PartyChallengePack> = emptyList()

    fun submitList(newItems: List<PartyChallengePack>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemPartyRecommendedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPartyRecommendedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvRecTitle.setText(item.nameRes)
        holder.binding.tvRecSubtitle.setText(item.descRes)
        holder.binding.ivRecIcon.setImageResource(item.iconRes)

        val context = holder.itemView.context
        if (item.tagResList.isNotEmpty()) {
            holder.binding.tvTag1.setText(item.tagResList[0])
            holder.binding.tvTag2.text = item.playersRange
            holder.binding.tvTag3.setText(item.tagResList.getOrElse(1) { com.phuongthanh.diceroller.R.string.difficulty_easy })
        } else if (item.tags.isNotEmpty()) {
            holder.binding.tvTag1.text = item.tags.getOrNull(0) ?: ""
            holder.binding.tvTag2.text = item.tags.getOrNull(1) ?: ""
            holder.binding.tvTag3.text = item.tags.getOrNull(2) ?: ""
        }

        holder.itemView.setOnDebouncedClickListener {
            onItemClick(item)
        }
        holder.binding.btnRecGo.setOnDebouncedClickListener {
            onItemClick(item)
        }
    }
}
