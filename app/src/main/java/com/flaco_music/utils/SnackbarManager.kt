package com.flaco_music.utils

import android.view.View
import com.flaco_music.R
import com.flaco_music.utils.resource.ResourceManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SnackbarManager : KoinComponent {

    var rootView: View? = null

    private val snackbarView: SnackbarView?
        get() = rootView?.findViewById(R.id.snackbarView)

    private val resourceManager: ResourceManager by inject()

    fun showSnackbar(text: String, snackbarType: SnackbarType = SnackbarType.UNDEFINED) {
        snackbarView?.show(
            text = text,
            iconResId = when (snackbarType) {
                SnackbarType.SUCCESS -> R.drawable.ic_baseline_check_circle_outline_24
                SnackbarType.ERROR -> R.drawable.ic_baseline_error_outline_24
                SnackbarType.UNDEFINED -> null
            },

            color = when (snackbarType) {
                SnackbarType.SUCCESS -> resourceManager.getColor(R.color.green)
                SnackbarType.ERROR -> resourceManager.getColor(R.color.red)
                SnackbarType.UNDEFINED -> 0
            }
        )
    }

    enum class SnackbarType {
        UNDEFINED, SUCCESS, ERROR
    }
}