package com.flaco_music.utils.extentions

import android.content.res.ColorStateList
import android.widget.TextView

fun TextView.setDrawable(drawableId: Int, color: Int) {
    setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0)
    compoundDrawableTintList = ColorStateList.valueOf(color)
}