package com.flaco_music.utils

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.flaco_music.databinding.SnackbarViewBinding
import com.flaco_music.utils.extentions.*
import kotlinx.coroutines.delay

class SnackbarView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val binding = SnackbarViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    ).also {
        removeAllViews()
        it.root.gone()
        addView(it.root)
    }

    fun show(text: String, iconResId: Int? = null, color: Int) {
        binding.text.text = text

        iconResId?.let {
            binding.icon.visible()
            binding.icon.setImageResource(iconResId)
            binding.icon.imageTintList = ColorStateList.valueOf(color)
            binding.indicatorView.visible()
            binding.indicatorView.backgroundTintList = ColorStateList.valueOf(color)
        }

        coroutineMain {
            binding.root.visibleWithFading()
            delay(3000)
            binding.root.goneWithFading()
            delay(500)
            binding.indicatorView.gone()
        }
    }
}