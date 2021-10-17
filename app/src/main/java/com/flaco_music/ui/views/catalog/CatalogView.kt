package com.flaco_music.ui.views.catalog

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding

abstract class CatalogView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected open var title: String = ""

    protected abstract val onShowAllClicked: () -> Unit

    protected abstract val binding: ViewBinding
}