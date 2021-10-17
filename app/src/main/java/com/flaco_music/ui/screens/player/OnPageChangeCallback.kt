package com.flaco_music.ui.screens.player

import androidx.viewpager2.widget.ViewPager2

class OnPageChangeCallback(
    private var initialPosition: Int,
    private val callback: (action: Action) -> Unit
) : ViewPager2.OnPageChangeCallback() {

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        val action = when (position) {
            initialPosition + 1 -> Action.NEXT
            initialPosition - 1 -> Action.PREVIOUS
            else -> return
        }

        callback(action)

        initialPosition = position
    }

    enum class Action {
        NEXT, PREVIOUS
    }
}