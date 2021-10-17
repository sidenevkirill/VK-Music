package com.flaco_music.ui.dialogs.playlist_options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.flaco_music.R
import com.flaco_music.databinding.DialogOptionsBinding
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.ui.adapters.options.OptionsAdapter
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.extentions.invoke
import com.flaco_music.utils.options.OptionsCallbackListener
import com.flaco_music.utils.options.OptionsUtils
import com.flaco_music.utils.options.OptionsUtils.OptionType
import com.flaco_music.utils.preferences.Preferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class PlaylistOptionsBottomSheetDialog(
    private val playlistId: Int = -1,
    private val ownerId: Int = -1,
    private val listener: OptionsCallbackListener? = null
) : BottomSheetDialogFragment(), KoinComponent {

    private var _binding: DialogOptionsBinding? = null
    private val binding get() = _binding!!

    private val audioRepository: AudioRepository by inject { parametersOf(false) }
    private val preferences: Preferences by inject()
    private val snackbarManager: SnackbarManager by inject()

    private val viewModel: PlaylistOptionsViewModel by viewModel()

    private val artistId: String?
        get() = playlist.mainArtists?.get(0)?.id

    private lateinit var playlist: Playlist

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioRepository.getPlaylist(ownerId, playlistId).invoke(
            onResponseSuccessful = { response ->
                try {
                    response.body()?.response?.playlist?.let {
                        playlist = it

                        val playlistOptions = OptionsUtils(requireContext()).createPlaylistOptions(
                            *mutableListOf(
                                Pair(OptionType.SHUFFLE_PLAY) {
                                    viewModel.shufflePlay(
                                        playlistId,
                                        response.body()?.response?.audios ?: listOf()
                                    )
                                    dismiss()
                                },
                                Pair(OptionType.SHARE_ID) { sharePlaylist() }
                            ).apply {
                                if (playlist.ownerId != preferences.userId) {
                                    if (it.isFollowing) {
                                        add(0, Pair(OptionType.UNFOLLOW_ID) { unfollowPlaylist() })
                                    } else {
                                        add(0, Pair(OptionType.FOLLOW_ID) { followPlaylist() })
                                    }
                                }
                                if (artistId != null) {
                                    add(lastIndex, Pair(OptionType.VIEW_ARTIST_ID) { viewArtist() })
                                }
                            }.toTypedArray()
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            binding.optionsRecycler.adapter = OptionsAdapter(playlistOptions)
                        }
                    }
                } catch (e: IllegalStateException) {

                }
            }
        )
    }

    private fun followPlaylist() {
        audioRepository.followPlaylist(ownerId, playlistId).invoke(
            onResponseSuccessful = {
                dismiss()
                listener?.onOptionSelected(OptionType.FOLLOW_ID)
                snackbarManager.showSnackbar(
                    getString(R.string.playlist_added),
                    SnackbarManager.SnackbarType.SUCCESS
                )
            },
            onResponseNotSuccessful = {
                snackbarManager.showSnackbar(getString(R.string.playlist_not_added), SnackbarManager.SnackbarType.ERROR)
            }
        )
    }

    private fun unfollowPlaylist() {
        val playlistFromMyMusic = runBlocking(Dispatchers.IO) {
            audioRepository.findPlaylistInMyMusic(
                preferences.userId,
                playlist.properPlaylistId
            )
        }
        if (playlistFromMyMusic != null) {
            audioRepository.removePlaylist(playlistFromMyMusic.ownerId, playlistFromMyMusic.id).invoke(
                onResponseSuccessful = {
                    snackbarManager.showSnackbar(
                        getString(R.string.playlist_removed),
                        SnackbarManager.SnackbarType.SUCCESS
                    )
                    dismiss()
                    listener?.onOptionSelected(OptionType.UNFOLLOW_ID)
                },
                onResponseNotSuccessful = {
                    snackbarManager.showSnackbar(
                        getString(R.string.playlist_not_removed),
                        SnackbarManager.SnackbarType.ERROR
                    )
                }
            )
        }
    }

    private fun viewArtist() {
        val arguments = Bundle().apply {
            putString("artistId", artistId)
        }
        requireActivity().findNavController(R.id.navHostFragment).navigate(R.id.artistFragment, arguments)
        listener?.onOptionSelected(OptionType.VIEW_ARTIST_ID)
        dismiss()
    }

    private fun sharePlaylist() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "${playlist.mainArtists?.get(0)?.name} - ${playlist.title}")
            putExtra(
                Intent.EXTRA_TEXT,
                "https://flaco.music/playlist/${playlist.properOwnerId}_${playlist.properPlaylistId}"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, playlist.title)
        startActivity(shareIntent)
        dismiss()
        listener?.onOptionSelected(OptionType.SHARE_ID)
    }

    companion object {
        const val TAG = "TrackOptionsBottomSheet"
    }
}