package com.flaco_music.ui.screens.catalog.tracks

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.databinding.FragmentCatalogTracksBinding
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.tracks.TracksAdapter
import com.flaco_music.utils.extentions.goneWithFading
import com.flaco_music.utils.extentions.setupWithStatusBar
import com.flaco_music.utils.extentions.visibleWithFading
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class CatalogTracksFragment : BaseFragment<FragmentCatalogTracksBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentCatalogTracksBinding =
        { inflater, container ->
            FragmentCatalogTracksBinding.inflate(inflater, container, false)
        }

    private val viewModel: CatalogTracksViewModel by viewModel()

    private val navArgs: CatalogTracksFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadTracks(navArgs.sectionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        observeViewModel()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.swipeRefreshLayout) {
            setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN)
            setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)
            setRefreshing(true)
            setOnRefreshListener {
                viewModel.loadTracks(navArgs.sectionId)
            }
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), binding.swipeRefreshLayout)
    }

    private fun observeViewModel() {
        viewModel.titleLiveData.observe(viewLifecycleOwner) {
            binding.title.text = it
        }
        viewModel.tracksLiveData.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                binding.swipeRefreshLayout.setRefreshing(false)
                delay(400)
                setupTracksRecycler(it)
            }
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupTracksRecycler(tracks: List<Audio>) {
        binding.tracksRecycler.setHasFixedSize(true)
        binding.tracksRecycler.adapter = TracksAdapter(
            tracks = tracks,
            lifecycleOwner = viewLifecycleOwner,
            onClick = { position -> viewModel.play(position) },
            onLongClick = { trackId, ownerId -> openTrackOptionsBottomSheetDialog(trackId, ownerId) }
        )
    }

    override fun showLoading() {
        binding.swipeRefreshLayout.setRefreshing(true)
        if (isFirstLaunch) {
            binding.tracksRecycler.goneWithFading()
        }
    }

    override fun hideLoading() {
        binding.swipeRefreshLayout.setRefreshing(false)
        if (isFirstLaunch) {
            binding.tracksRecycler.visibleWithFading()
            isFirstLaunch = false
        }
    }
}