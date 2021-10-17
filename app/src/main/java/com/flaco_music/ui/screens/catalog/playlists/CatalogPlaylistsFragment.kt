package com.flaco_music.ui.screens.catalog.playlists

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.databinding.FragmentCatalogPlaylistsBinding
import com.flaco_music.retrofit.models.CatalogSectionPlaylist
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.catalog.playlists.CatalogPlaylistsSectionAdapter
import com.flaco_music.utils.extentions.goneWithFading
import com.flaco_music.utils.extentions.setupWithStatusBar
import com.flaco_music.utils.extentions.visibleWithFading
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class CatalogPlaylistsFragment : BaseFragment<FragmentCatalogPlaylistsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentCatalogPlaylistsBinding =
        { inflater, container ->
            FragmentCatalogPlaylistsBinding.inflate(inflater, container, false)
        }

    private val viewModel: CatalogPlaylistsViewModel by viewModel()

    private val navArgs: CatalogPlaylistsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadPlaylists(navArgs.sectionId)
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
                viewModel.loadPlaylists(navArgs.sectionId)
            }
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), binding.swipeRefreshLayout)
    }

    private fun observeViewModel() {
        viewModel.titleLiveData.observe(viewLifecycleOwner) {
            binding.title.text = it
        }
        viewModel.playlistsLiveData.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                binding.swipeRefreshLayout.setRefreshing(false)
                delay(400)
                setupPlaylistsRecycler(it)
            }
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupPlaylistsRecycler(playlists: List<CatalogSectionPlaylist>) {
        val adapter = CatalogPlaylistsSectionAdapter(
            playlists = playlists,
            onClick = { playlistId, ownerId ->
                openPlaylistFragment(playlistId, ownerId)
            },
            onLongClick = { playlistId, ownerId ->
                openPlaylistOptionsBottomSheetDialog(playlistId, ownerId)
            })
        binding.playlistsRecycler.adapter = adapter
    }

    private fun openPlaylistFragment(playlistId: Int, ownerId: Int) {
        val action =
            CatalogPlaylistsFragmentDirections.actionCatalogPlaylistsFragmentToPlaylistFragment(playlistId, ownerId)
        navigateTo(action)
    }

    override fun showLoading() {
        binding.swipeRefreshLayout.setRefreshing(true)
        if (isFirstLaunch) {
            binding.playlistsRecycler.goneWithFading()
        }
    }

    override fun hideLoading() {
        binding.swipeRefreshLayout.setRefreshing(false)
        if (isFirstLaunch) {
            binding.playlistsRecycler.visibleWithFading()
            isFirstLaunch = false
        }
    }
}