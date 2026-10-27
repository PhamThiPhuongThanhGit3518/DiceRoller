package com.phuongthanh.diceroller.core.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

import com.phuongthanh.diceroller.DiceRollerApplication

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding ?: throw IllegalStateException("Binding accessed outside lifecycle")
    protected val bindingOrNull: VB? get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater(layoutInflater)
        setContentView(binding.root)
        DiceRollerApplication.hideSystemBars(this)
        initViews()
        initObservers()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            DiceRollerApplication.hideSystemBars(this)
        }
    }

    protected open fun initViews() {}
    protected open fun initObservers() {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
