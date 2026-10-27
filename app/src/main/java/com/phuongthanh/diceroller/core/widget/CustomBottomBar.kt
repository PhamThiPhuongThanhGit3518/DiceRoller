package com.phuongthanh.diceroller.core.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.phuongthanh.diceroller.R

enum class BottomTab {
    HOME,
    DICE,
    ROULETTE
}

class CustomBottomBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var currentTab: BottomTab = BottomTab.HOME
    private var onTabSelectedListener: ((BottomTab) -> Unit)? = null
    private var isNavigating = false

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        renderTabs()
    }

    fun setOnTabSelectedListener(listener: (BottomTab) -> Unit) {
        this.onTabSelectedListener = listener
    }

    fun setSelectedTab(tab: BottomTab, notify: Boolean = true) {
        if (currentTab == tab) return
        currentTab = tab
        renderTabs()
        if (notify) {
            onTabSelectedListener?.invoke(tab)
        }
    }

    fun getSelectedTab(): BottomTab = currentTab

    fun setupWithNavController(navController: NavController) {
        this.setOnTabSelectedListener { tab ->
            if (isNavigating) return@setOnTabSelectedListener
            isNavigating = true
            when (tab) {
                BottomTab.HOME -> {
                    navController.popBackStack(R.id.homeFragment, false)
                }
                BottomTab.DICE -> {
                    navController.navigate(R.id.selectGameModeFragment)
                }
                BottomTab.ROULETTE -> {
                    navController.navigate(R.id.rouletteFragment)
                }
            }
            postDelayed({ isNavigating = false }, 300L)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    visibility = View.VISIBLE
                    if (currentTab != BottomTab.HOME) {
                        currentTab = BottomTab.HOME
                        renderTabs()
                    }
                }
                R.id.selectGameModeFragment,
                R.id.classicPlayerFragment,
                R.id.freeRollFragment -> {
                    visibility = View.VISIBLE
                    if (currentTab != BottomTab.DICE) {
                        currentTab = BottomTab.DICE
                        renderTabs()
                    }
                }
                R.id.rouletteFragment -> {
                    visibility = View.VISIBLE
                    if (currentTab != BottomTab.ROULETTE) {
                        currentTab = BottomTab.ROULETTE
                        renderTabs()
                    }
                }
                R.id.rouletteSpinFragment,
                R.id.createWheelFragment,
                R.id.editWheelFragment,
                R.id.customRollPlayFragment,
                R.id.diceSettingsFragment,
                R.id.customRollHomeFragment,
                R.id.customDiceStep1Fragment,
                R.id.customDiceStep2Fragment,
                R.id.partyDetailFragment,
                R.id.partyRollPlayFragment -> {
                    visibility = View.GONE
                }
                else -> {
                    visibility = View.VISIBLE
                }
            }
        }
    }

    private fun renderTabs() {
        removeAllViews()
        val density = resources.displayMetrics.density
        val dp8 = (8 * density).toInt()

        when (currentTab) {
            BottomTab.HOME -> {
                // Home active (Circle on left) + Pill with Dice & Roulette on right
                addView(createActiveCircle(BottomTab.HOME))
                addView(createPillContainer(listOf(BottomTab.DICE, BottomTab.ROULETTE), marginStart = dp8))
            }
            BottomTab.DICE -> {
                // Pill with Home on left + Circle Dice in center + Pill with Roulette on right
                addView(createPillContainer(listOf(BottomTab.HOME), marginEnd = dp8))
                addView(createActiveCircle(BottomTab.DICE))
                addView(createPillContainer(listOf(BottomTab.ROULETTE), marginStart = dp8))
            }
            BottomTab.ROULETTE -> {
                // Pill with Home & Dice on left + Circle Roulette on right
                addView(createPillContainer(listOf(BottomTab.HOME, BottomTab.DICE), marginEnd = dp8))
                addView(createActiveCircle(BottomTab.ROULETTE))
            }
        }
    }

    private fun createActiveCircle(tab: BottomTab): View {
        val density = resources.displayMetrics.density
        val size52dp = (52 * density).toInt()
        val iconSize26dp = (26 * density).toInt()

        val frame = FrameLayout(context).apply {
            layoutParams = LayoutParams(size52dp, size52dp)
            background = ContextCompat.getDrawable(context, R.drawable.bg_circle_nav_active)
            elevation = 6 * density
        }

        val icon = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(iconSize26dp, iconSize26dp, Gravity.CENTER)
            setImageResource(getIconRes(tab))
            setColorFilter(Color.WHITE)
        }

        frame.addView(icon)
        return frame
    }

    private fun createPillContainer(tabs: List<BottomTab>, marginStart: Int = 0, marginEnd: Int = 0): View {
        val density = resources.displayMetrics.density
        val height52dp = (52 * density).toInt()
        val padHorizontal = (12 * density).toInt()

        val pill = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, height52dp).apply {
                this.marginStart = marginStart
                this.marginEnd = marginEnd
            }
            background = ContextCompat.getDrawable(context, R.drawable.bg_bottom_nav_pill)
            elevation = 3 * density
            setPadding(padHorizontal, 0, padHorizontal, 0)
        }

        for (tab in tabs) {
            val itemView = createInactiveTabItem(tab)
            pill.addView(itemView)
        }

        return pill
    }

    private fun createInactiveTabItem(tab: BottomTab): View {
        val density = resources.displayMetrics.density
        val pad8dp = (8 * density).toInt()
        val iconSize22dp = (22 * density).toInt()

        val itemLayout = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                marginStart = pad8dp
                marginEnd = pad8dp
            }
            setOnClickListener {
                setSelectedTab(tab, notify = true)
            }
        }

        val icon = ImageView(context).apply {
            layoutParams = LayoutParams(iconSize22dp, iconSize22dp)
            setImageResource(getIconRes(tab))
            setColorFilter(Color.parseColor("#667085"))
        }

        val label = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (2 * density).toInt()
            }
            text = getTitle(tab)
            textSize = 10.5f
            setTextColor(Color.parseColor("#475467"))
            isSingleLine = true
        }

        itemLayout.addView(icon)
        itemLayout.addView(label)
        return itemLayout
    }

    private fun getIconRes(tab: BottomTab): Int = when (tab) {
        BottomTab.HOME -> R.drawable.ic_nav_home
        BottomTab.DICE -> R.drawable.ic_nav_dice
        BottomTab.ROULETTE -> R.drawable.ic_nav_roulette
    }

    private fun getTitle(tab: BottomTab): String = when (tab) {
        BottomTab.HOME -> context.getString(R.string.tab_home)
        BottomTab.DICE -> context.getString(R.string.tab_dice_master)
        BottomTab.ROULETTE -> context.getString(R.string.tab_roulette)
    }
}
