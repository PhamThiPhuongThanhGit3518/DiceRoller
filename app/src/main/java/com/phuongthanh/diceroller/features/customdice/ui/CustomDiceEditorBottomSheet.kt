package com.phuongthanh.diceroller.features.customdice.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phuongthanh.diceroller.R
import com.phuongthanh.diceroller.core.utils.setOnDebouncedClickListener
import com.phuongthanh.diceroller.databinding.BottomSheetCustomDiceEditorBinding
import com.phuongthanh.diceroller.databinding.ItemCustomDiceFaceEditorBinding
import com.phuongthanh.diceroller.features.customdice.model.CustomDice
import com.phuongthanh.diceroller.features.customdice.model.CustomDiceFace
import java.util.UUID

class CustomDiceEditorBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCustomDiceEditorBinding? = null
    private val binding get() = _binding!!

    var existingDice: CustomDice? = null
    var onSaveDice: ((CustomDice) -> Unit)? = null

    private val availableColors = listOf(
        "#EF4444", "#F59E0B", "#3B82F6", "#8B5CF6", "#10B981", "#EC4899",
        "#06B6D4", "#F97316", "#84CC16", "#6366F1", "#A855F7", "#14B8A6"
    )

    data class EditableFace(
        var index: Int,
        var text: String,
        var colorHex: String
    )

    private val facesList = mutableListOf<EditableFace>()
    private lateinit var facesAdapter: FacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCustomDiceEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        facesAdapter = FacesAdapter()
        binding.rvFaces.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFaces.adapter = facesAdapter

        initializeData()
        setupQuickDSelectors()
        setupActions()
    }

    private fun initializeData() {
        existingDice?.let { dice ->
            binding.etDiceTitle.setText(dice.title)
            facesList.clear()
            dice.faces.forEachIndexed { i, face ->
                facesList.add(
                    EditableFace(
                        index = i + 1,
                        text = face.text,
                        colorHex = face.colorHex
                    )
                )
            }
        } ?: run {
            // Default to 6 faces for new dice
            setFacesCount(6)
        }
        updateFacesHeader()
        facesAdapter.notifyDataSetChanged()
    }

    private fun setFacesCount(count: Int) {
        val currentTitle = binding.etDiceTitle.text?.toString()?.trim().orEmpty()
        facesList.clear()
        for (i in 1..count) {
            val color = availableColors[(i - 1) % availableColors.size]
            facesList.add(
                EditableFace(
                    index = i,
                    text = "Face $i",
                    colorHex = color
                )
            )
        }
        updateFacesHeader()
        facesAdapter.notifyDataSetChanged()
    }

    private fun setupQuickDSelectors() {
        binding.btnQuickD4.setOnClickListener { setFacesCount(4) }
        binding.btnQuickD6.setOnClickListener { setFacesCount(6) }
        binding.btnQuickD8.setOnClickListener { setFacesCount(8) }
        binding.btnQuickD10.setOnClickListener { setFacesCount(10) }
        binding.btnQuickD12.setOnClickListener { setFacesCount(12) }
        binding.btnQuickD20.setOnClickListener { setFacesCount(20) }
    }

    private fun setupActions() {
        binding.btnAddFace.setOnDebouncedClickListener {
            val nextIndex = facesList.size + 1
            val color = availableColors[(nextIndex - 1) % availableColors.size]
            facesList.add(
                EditableFace(
                    index = nextIndex,
                    text = "Face $nextIndex",
                    colorHex = color
                )
            )
            updateFacesHeader()
            facesAdapter.notifyItemInserted(facesList.size - 1)
            binding.rvFaces.smoothScrollToPosition(facesList.size - 1)
        }

        binding.btnCancel.setOnDebouncedClickListener {
            dismiss()
        }

        binding.btnSaveDice.setOnDebouncedClickListener {
            val title = binding.etDiceTitle.text?.toString()?.trim().orEmpty()
            if (title.isEmpty()) {
                binding.layoutDiceTitle.error = getString(R.string.dice_title_hint)
                return@setOnDebouncedClickListener
            }

            if (facesList.size < 2) {
                Toast.makeText(requireContext(), R.string.min_faces_warning, Toast.LENGTH_SHORT).show()
                return@setOnDebouncedClickListener
            }

            val resultFaces = facesList.mapIndexed { idx, f ->
                CustomDiceFace(
                    faceIndex = idx + 1,
                    text = f.text.ifEmpty { "Face ${idx + 1}" },
                    colorHex = f.colorHex
                )
            }

            val diceId = existingDice?.id ?: UUID.randomUUID().toString()
            val newDice = CustomDice(
                id = diceId,
                title = title,
                faces = resultFaces,
                isPreset = false // Saved as user editable
            )

            onSaveDice?.invoke(newDice)
            dismiss()
        }
    }

    private fun updateFacesHeader() {
        binding.tvFacesCount.text = getString(R.string.faces_count_format, facesList.size)
    }

    inner class FacesAdapter : RecyclerView.Adapter<FacesAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemCustomDiceFaceEditorBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemCustomDiceFaceEditorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun getItemCount(): Int = facesList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = facesList[position]
            holder.itemBinding.tvFaceNumber.text = "#${position + 1}"

            // Detach previous text watcher if any by resetting
            holder.itemBinding.etFaceText.setText(item.text)
            holder.itemBinding.etFaceText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val pos = holder.bindingAdapterPosition
                    if (pos in facesList.indices) {
                        facesList[pos].text = s?.toString().orEmpty()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // Color badge
            val colorBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                try {
                    setColor(Color.parseColor(item.colorHex))
                } catch (_: Exception) {
                    setColor(Color.parseColor("#3B82F6"))
                }
            }
            holder.itemBinding.viewColorBadge.background = colorBg

            // Tapping color cycles through available colors
            holder.itemBinding.viewColorBadge.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos in facesList.indices) {
                    val currentHex = facesList[pos].colorHex
                    val currentIdx = availableColors.indexOf(currentHex)
                    val nextColor = availableColors[(currentIdx + 1) % availableColors.size]
                    facesList[pos].colorHex = nextColor
                    notifyItemChanged(pos)
                }
            }

            // Remove button: only visible if faces > 2
            holder.itemBinding.btnRemoveFace.visibility = if (facesList.size > 2) View.VISIBLE else View.INVISIBLE
            holder.itemBinding.btnRemoveFace.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos in facesList.indices && facesList.size > 2) {
                    facesList.removeAt(pos)
                    // Re-index remaining faces
                    facesList.forEachIndexed { idx, face -> face.index = idx + 1 }
                    updateFacesHeader()
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CustomDiceEditorBottomSheet"

        fun newInstance(dice: CustomDice? = null): CustomDiceEditorBottomSheet {
            return CustomDiceEditorBottomSheet().apply {
                existingDice = dice
            }
        }
    }
}
