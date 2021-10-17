package com.flaco_music.ui.screens.playlist

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.BlurTransformation
import com.flaco_music.R
import com.flaco_music.databinding.FragmentPlaylistBinding
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.GetPlaylistResponseBody
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.tracks.TracksAdapter
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.extentions.*
import com.flaco_music.utils.options.OptionsCallbackListener
import com.flaco_music.utils.options.OptionsUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class PlaylistFragment : BaseFragment<FragmentPlaylistBinding>(), OptionsCallbackListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentPlaylistBinding = { inflater, container ->
        FragmentPlaylistBinding.inflate(inflater, container, false)
    }

    private val viewModel: PlaylistViewModel by viewModel()
    private val navArgs: PlaylistFragmentArgs by navArgs()

    private val playlistId: Int by lazy { navArgs.playlistId }
    private val ownerId: Int by lazy { navArgs.ownerId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadPlaylistData(playlistId, ownerId)
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

        binding.optionsImage.onClick {
            (requireActivity() as MainActivity).openPlaylistOptionsBottomSheetDialog(
                playlistId = playlistId,
                ownerId = ownerId
            )
        }

        binding.shufflePlayFab.onClick {
            GlobalScope.launch(Dispatchers.IO) {
                viewModel.onShufflePlayClicked()
                withContext(Dispatchers.Main) {
                    binding.tracksRecycler.adapter?.notifyDataSetChanged()
                }
            }
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), null, Color.BLACK)
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
        if (optionType == OptionsUtils.OptionType.REMOVE_FROM_PLAYLIST_ID) {
            viewModel.loadPlaylistData(playlistId, ownerId)
        }
    }

    private fun observeViewModel() {
        viewModel.playlistDataLiveData.observe(viewLifecycleOwner) {
            setupPlaylistData(it)
        }
        viewModel.tracksLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                setupTracksRecycler(it)
                binding.shufflePlayFab.visibleWithFading()
                binding.playlistIsEmptyText.gone()
            } else {
                binding.shufflePlayFab.gone()
                binding.playlistIsEmptyText.visibleWithFading()
            }
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupPlaylistData(playlistBody: GetPlaylistResponseBody) {
        val playlist = playlistBody.playlist

        val builder: ImageRequest.Builder.() -> Unit = {
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
        }

        if (playlist.photo?.photo600 != null) {
            binding.coverImage.load(playlist.photo.photo600, builder = builder)
        } else {
            binding.coverImage.load(R.drawable.playlist_placeholder_big, builder = builder)
        }

        val blurredBuilder: ImageRequest.Builder.() -> Unit = {
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
            transformations(BlurTransformation(requireContext(), 15f, 0.7f))
        }
        if (playlist.photo?.photo300 != null) {
            binding.coverImageBlurred.load(playlist.photo.photo300, builder = blurredBuilder)
        } else {
            binding.coverImageBlurred.load(R.drawable.playlist_placeholder_big, builder = blurredBuilder)
        }

        binding.playlistNameText.text = playlist.title

        binding.artistsNamesText.text = playlist.mainArtists?.map { it.name }?.let {
            binding.artistsNamesText.visible()
            TextUtils.join(", ", it)
        } ?: run {
            binding.artistsNamesText.gone()
            ""
        }

        val plays = playlist.plays
        val playsK = (plays - (plays % 1000)) / 1000
        val playsM = (plays - (plays % 1000000)) / 1000000
        val playsB = (plays - (plays % 1000000000)) / 1000000000
        val penultimatePlays: Char = try {
            plays.toString()[plays.toString().lastIndex - 1]
        } catch (e: StringIndexOutOfBoundsException) {
            ' '
        }
        val playsText = when {
            playsK in 1..999 -> getString(R.string.additional_playlist_info_listens_k, playsK)
            playsM in 1..999 -> getString(R.string.additional_playlist_info_listens_m, playsM)
            playsB in 1..999 -> getString(R.string.additional_playlist_info_listens_b, playsB)
            else -> if (penultimatePlays != '1') {
                when (plays.toString().last()) {
                    '1' -> getString(R.string.additional_playlist_info_listens_less_k_one, plays)
                    '2',
                    '3',
                    '4' -> getString(R.string.additional_playlist_info_listens_less_k_two_three_four, plays)
                    else -> getString(R.string.additional_playlist_info_listens_less_k_many, plays)
                }
            } else {
                getString(R.string.additional_playlist_info_listens_less_k_many, plays)
            }
        }

        val audios = playlistBody.audios

        val hours = audios.sumOf { it.duration }.div(3600)
        val hoursText = if (hours >= 1) {
            when (hours.toString().last()) {
                '1' -> getString(R.string.additional_playlist_info_hours_one, hours) + " "
                '2',
                '3',
                '4' -> getString(R.string.additional_playlist_info_hours_two_three_four, hours) + " "
                else -> getString(R.string.additional_playlist_info_hours_many, hours) + " "
            }
        } else ""

        val minutes = audios.sumOf { it.duration }.div(60) - (60 * hours)
        val penultimateMinutes: Char = try {
            minutes.toString()[minutes.toString().lastIndex - 1]
        } catch (e: StringIndexOutOfBoundsException) {
            ' '
        }

        val minutesText = if (penultimateMinutes != '1') {
            when (minutes.toString().last()) {
                '1' -> getString(R.string.additional_playlist_info_minutes_one, minutes)
                '2',
                '3',
                '4' -> getString(R.string.additional_playlist_info_minutes_two_three_four, minutes)
                else -> getString(R.string.additional_playlist_info_minutes_many, minutes)
            }
        } else {
            getString(R.string.additional_playlist_info_minutes_many, minutes)
        }

        binding.additionalInfoText.text = getString(
            R.string.additional_playlist_info,
            playsText,
            "$hoursText$minutesText"
        )
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
                    if (viewModel.isMyPlaylist) playlistId else null,
                    this
                )
            }
        )
    }

    override fun showLoading() {
        binding.progressBarContainer.visibleWithFading()
    }

    override fun hideLoading() {
        binding.progressBarContainer.goneWithFading()
    }
}