package com.flaco_music.ui.screens.my_music

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.databinding.FragmentMyMusicBinding
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.playlists.PlaylistsAdapter
import com.flaco_music.ui.adapters.tracks.TracksAdapter
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.extentions.*
import com.flaco_music.utils.options.OptionsCallbackListener
import com.flaco_music.utils.options.OptionsUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension


@KoinApiExtension
class MyMusicFragment : BaseFragment<FragmentMyMusicBinding>(), OptionsCallbackListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentMyMusicBinding = { inflater, container ->
        FragmentMyMusicBinding.inflate(inflater, container, false)
    }

    private val viewModel: MyMusicViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadMusicPage(isForcibly = false)
        Log.d(TAG, "onCreate: ")
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

        binding.appBarLayout.setExpanded(!(viewModel.isScrolledLiveData.value ?: true))

        with(binding.swipeRefreshLayout) {
            setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN)
            setColorSchemeColors(Color.WHITE)
            setOnRefreshListener {
                viewModel.loadMusicPage(isForcibly = true)
            }
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), binding.swipeRefreshLayout)
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            viewModel.setScrolled(verticalOffset < 0)
        })
    }

    override fun onOptionSelected(optionType: OptionsUtils.OptionType) {
        when (optionType) {
            OptionsUtils.OptionType.REMOVE_FROM_MY_MUSIC_ID -> viewModel.loadMusicPage(isForcibly = true, delay = 2000)
            OptionsUtils.OptionType.UNFOLLOW_ID -> viewModel.loadMusicPage(isForcibly = true, delay = 2000)
            else -> return
        }
    }

    private fun observeViewModel() {
        viewModel.playlistsLiveData.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                binding.swipeRefreshLayout.setRefreshing(false)
                setupPlaylistsRecycler(it)
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
        if (playlists.isNotEmpty()) {
            binding.playlistsRecycler.visible()
            binding.playlistsRecycler.adapter = PlaylistsAdapter(
                playlists = playlists,
                onClick = { playlistId, ownerId -> openPlaylistFragment(playlistId, ownerId) },
                onLongClick = { playlistId, ownerId ->
                    (requireActivity() as MainActivity).openPlaylistOptionsBottomSheetDialog(playlistId, ownerId, this)
                }
            )
        } else {
            binding.playlistsRecycler.adapter = null
            binding.playlistsRecycler.gone()
        }
    }

    private fun setupTracksRecycler(tracks: List<Audio>) {
        binding.tracksRecycler.adapter = TracksAdapter(
            tracks = tracks,
            lifecycleOwner = viewLifecycleOwner,
            onClick = { position -> viewModel.play(position) },
            onLongClick = { trackId, ownerId ->
                openTrackOptionsBottomSheetDialog(
                    trackId,
                    ownerId,
                    null,
                    this
                )
            }
        )
    }

    private fun openPlaylistFragment(playlistId: Int, ownerId: Int) {
        val action = MyMusicFragmentDirections.actionMyMusicFragmentToPlaylistFragment(playlistId, ownerId)
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

    companion object {
        const val TAG = "MyMusicFragment"
    }
}