package com.phuongthanh.diceroller.features.intro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phuongthanh.diceroller.databinding.ItemOnboardingSlideBinding

class IntroSlideAdapter(
    private val slides: List<IntroSlide>
) : RecyclerView.Adapter<IntroSlideAdapter.SlideViewHolder>() {

    inner class SlideViewHolder(val binding: ItemOnboardingSlideBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemOnboardingSlideBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        val slide = slides[position]
        holder.binding.ivIllustration.setImageResource(slide.illustrationRes)
    }

    override fun getItemCount(): Int = slides.size
}
