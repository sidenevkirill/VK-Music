package com.flaco_music.ui.screens.friend_music

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.R
import com.flaco_music.databinding.FragmentFriendMusicBinding
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.playlists.PlaylistsAdapter
import com.flaco_music.ui.adapters.tracks.TracksAdapter
import com.flaco_music.utils.extentions.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class FriendMusicFragment : BaseFragment<FragmentFriendMusicBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentFriendMusicBinding = { inflater, container ->
        FragmentFriendMusicBinding.inflate(inflater, container, false)
    }

    private val viewModel: FriendMusicViewModel by viewModel()

    private val navArgs: FriendMusicFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadMusicPage(navArgs.ownerId)
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
                viewModel.loadMusicPage(navArgs.ownerId)
            }
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), binding.swipeRefreshLayout)
    }

    private fun observeViewModel() {
        viewModel.friendMusicInfoLiveData.observe(viewLifecycleOwner) {
            binding.title.text = it.name
            binding.numberOfTracksText.text = getString(R.string.number_of_tracks_pattern).format(it.numberOfTracks)
        }
        viewModel.playlistsLiveData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.playlistsRecycler.gone()
            } else {
                GlobalScope.launch(Dispatchers.Main) {
                    binding.swipeRefreshLayout.setRefreshing(false)
                    delay(400)
                    setupPlaylistsRecycler(it)
                }
            }
        }
        viewModel.tracksLiveData.observe(viewLifecycleOwner) {
            setupTracksRecycler(it)
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupPlaylistsRecycler(playlists: List<Playlist>) {
        binding.playlistsRecycler.adapter = PlaylistsAdapter(
            playlists = playlists,
            onClick = { playlistId, ownerId -> openPlaylistFragment(playlistId, ownerId) },
            onLongClick = { playlistId, ownerId -> openPlaylistOptionsBottomSheetDialog(playlistId, ownerId) }
        )
    }

    private fun setupTracksRecycler(tracks: List<Audio>) {
        binding.tracksRecycler.adapter = TracksAdapter(
            tracks = tracks,
            lifecycleOwner = viewLifecycleOwner,
            onClick = { position -> viewModel.play(position) },
            onLongClick = { trackId, ownerId -> openTrackOptionsBottomSheetDialog(trackId, ownerId) }
        )
    }

    private fun openPlaylistFragment(playlistId: Int, ownerId: Int) {
        val action = FriendMusicFragmentDirections.actionFriendMusicFragmentToPlaylistFragment(playlistId, ownerId)
        navigateTo(action)
    }

    override fun showLoading() {
        binding.swipeRefreshLayout.setRefreshing(true)
        if (isFirstLaunch) {
            binding.playlistsRecycler.invisibleWithFading()
            binding.tracksRecycler.goneWithFading()
        }
    }

    override fun hideLoading() {
        binding.swipeRefreshLayout.setRefreshing(false)
        if (isFirstLaunch) {
            binding.playlistsRecycler.visibleWithFading()
            binding.tracksRecycler.visibleWithFading()
            isFirstLaunch = false
        }
    }
}