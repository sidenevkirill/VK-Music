package com.flaco_music.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.options.OptionsCallbackListener

abstract class BaseFragment<V : ViewBinding> : Fragment() {

    private var _binding: ViewBinding? = null

    abstract val bindingInflater: (LayoutInflater, ViewGroup?) -> V

    protected var isFirstLaunch = true

    private val navController: NavController
        get() = requireView().findNavController()

    @Suppress("UNCHECKED_CAST")
    val binding: V
        get() = _binding as V

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = bindingInflater.invoke(inflater, container)
        return requireNotNull(_binding).root
    }

    protected fun navigateTo(action: NavDirections) {
        try {
            navController.navigate(action)
        } catch (e: IllegalArgumentException) {

        }
    }

    protected fun openTrackOptionsBottomSheetDialog(
        trackId: Int,
        ownerId: Int,
        playlistId: Int? = null,
        listener: OptionsCallbackListener? = null
    ) {
        (requireActivity() as MainActivity).openTrackOptionsBottomSheetDialog(
            trackId,
            ownerId,
            playlistId,
            listener
        )
    }

    protected fun openPlaylistOptionsBottomSheetDialog(
        playlistId: Int,
        ownerId: Int,
        listener: OptionsCallbackListener? = null
    ) {
        (requireActivity() as MainActivity).openPlaylistOptionsBottomSheetDialog(playlistId, ownerId, listener)
    }

    protected open fun showLoading() {
        // Do nothing
    }

    protected open fun hideLoading() {
        // Do nothing
    }
}