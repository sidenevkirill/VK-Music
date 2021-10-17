package com.flaco_music.utils.resource

import android.content.Context
import androidx.core.content.ContextCompat

class ResourceManager(private val context: Context) {

    fun getColor(colorId: Int): Int {
        return ContextCompat.getColor(context, colorId)
    }
}