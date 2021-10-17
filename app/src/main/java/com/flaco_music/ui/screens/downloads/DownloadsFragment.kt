package com.flaco_music.ui.screens.downloads

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.flaco_music.R
import com.flaco_music.databinding.FragmentDownloadsBinding
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.downloads.DownloadsAdapter
import com.flaco_music.utils.extentions.gone
import com.flaco_music.utils.extentions.goneWithFading
import com.flaco_music.utils.extentions.setupWithStatusBar
import com.flaco_music.utils.extentions.visibleWithFading
import com.flaco_music.utils.options.OptionsCallbackListener
import com.flaco_music.utils.options.OptionsUtils
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class DownloadsFragment : BaseFragment<FragmentDownloadsBinding>(), OptionsCallbackListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentDownloadsBinding = { inflater, container ->
        FragmentDownloadsBinding.inflate(inflater, container, false)
    }

    private val viewModel: DownloadsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        observeViewModel()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadTracks()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.apply {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.background)
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.apply {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
    }

    override fun onOptionSelected(optionType: OptionsUtils.OptionType) {
        when (optionType) {
            OptionsUtils.OptionType.REMOVE_FROM_SAVED_ID -> viewModel.loadTracks()
            else -> return
        }
    }

    private fun observeViewModel() {
        viewModel.tracksLiveData.observe(viewLifecycleOwner) { tracks ->
            binding.appBarLayout.setupWithStatusBar(requireActivity())
            binding.downloadsRecycler.adapter = DownloadsAdapter(
                tracks = tracks,
                lifecycleOwner = viewLifecycleOwner,
                onClick = { position -> viewModel.play(position, true) },
                onLongClick = { trackId, ownerId -> openTrackOptionsBottomSheetDialog(trackId, ownerId, null, this) }
            )
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.noDownloadsLiveData.observe(viewLifecycleOwner) {
            if (it) showNoDownloadsText() else hideNoDownloadsText()
        }
    }

    private fun showNoDownloadsText() {
        binding.downloadsRecycler.gone()
        binding.appBarLayout.gone()
        binding.noSavedTracksText.visibleWithFading()
    }

    private fun hideNoDownloadsText() {
        binding.noSavedTracksText.goneWithFading()
        binding.downloadsRecycler.visibleWithFading()
        binding.appBarLayout.visibleWithFading()
    }

    override fun showLoading() {
        binding.noSavedTracksText.goneWithFading()
        binding.progressBar.visibleWithFading()
        binding.downloadsRecycler.goneWithFading()
    }

    override fun hideLoading() {
        binding.progressBar.goneWithFading()
        binding.downloadsRecycler.visibleWithFading()
    }
}