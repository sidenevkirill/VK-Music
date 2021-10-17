package com.flaco_music.ui.screens.artist

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.flaco_music.R
import com.flaco_music.databinding.FragmentArtistBinding
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.playlists.PlaylistsAdapter
import com.flaco_music.ui.adapters.tracks.TracksAdapter
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.extentions.*
import com.google.android.material.snackbar.Snackbar
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension


@KoinApiExtension
class ArtistFragment : BaseFragment<FragmentArtistBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentArtistBinding = { inflater, container ->
        FragmentArtistBinding.inflate(inflater, container, false)
    }

    private val viewModel: ArtistViewModel by viewModel()

    private val navArgs: ArtistFragmentArgs by navArgs()

    private val artistId: String get() = navArgs.artistId

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
        viewModel.loadArtistData(artistId)

        binding.optionsImage.onClick {
            (requireActivity() as MainActivity).openArtistOptionsBottomSheetDialog(
                artistId = artistId,
            )
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), null, Color.BLACK)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.progressBarContainer.visible()
        binding.appBarLayout.visible()
        binding.tracksRecycler.visible()
        binding.artistNotFoundText.gone()
    }

    private fun observeViewModel() {
        viewModel.artistInfoLiveData.observe(viewLifecycleOwner) {
            setupArtistInfo(it)
        }
        viewModel.albumsLiveData.observe(viewLifecycleOwner) {
            setupPlaylistsRecycler(it)
        }
        viewModel.tracksLiveData.observe(viewLifecycleOwner) {
            setupTracksRecycler(it)
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.artistNotFoundLiveData.observe(viewLifecycleOwner) {
            if (it) showArtistNotFundText() else hideArtistNotFundText()
        }
        viewModel.noAlbumsLiveDataLiveData.observe(viewLifecycleOwner) {
            if (it) binding.playlistsRecycler.gone() else binding.playlistsRecycler.visible()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupArtistInfo(artistInfo: ArtistInfo) {
        binding.title.text = artistInfo.name
        binding.numberOfTracksText.text = getString(R.string.number_of_tracks_pattern).format(artistInfo.numberOfTracks)

        val builder: ImageRequest.Builder.() -> Unit = {
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
        }

        if (artistInfo.photoUrl != null) {
            binding.avatarImage.load(uri = artistInfo.photoUrl, builder = builder)
        } else {
            binding.avatarImage.load(drawableResId = R.drawable.artist_placeholder_big, builder = builder)
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
        val action = ArtistFragmentDirections.actionArtistFragmentToPlaylistFragment(playlistId, ownerId)
        navigateTo(action)
    }

    private fun showArtistNotFundText() {
        binding.progressBarContainer.gone()
        binding.appBarLayout.gone()
        binding.tracksRecycler.gone()
        binding.artistNotFoundText.visible()
    }

    private fun hideArtistNotFundText() {
        binding.artistNotFoundText.gone()
        binding.appBarLayout.visibleWithFading()
        binding.tracksRecycler.visibleWithFading()
    }

    override fun showLoading() {
        binding.artistNotFoundText.gone()
        binding.progressBarContainer.visibleWithFading()
    }

    override fun hideLoading() {
        binding.progressBarContainer.goneWithFading()
        binding.appBarLayout.visibleWithFading()
        binding.tracksRecycler.visibleWithFading()
    }
}