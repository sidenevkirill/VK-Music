package com.flaco_music.ui.dialogs.track_options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.flaco_music.R
import com.flaco_music.databinding.DialogOptionsBinding
import com.flaco_music.db.downloads.DownloadsRepository
import com.flaco_music.db.my_music_ids.MyMusicIdItem
import com.flaco_music.db.my_music_ids.MyMusicIdsRepository
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.ui.adapters.options.OptionsAdapter
import com.flaco_music.ui.dialogs.add_track_to_playlist.AddTrackToPlaylistBottomSheetDialog
import com.flaco_music.ui.dialogs.lyrics.LyricsBottomSheetDialog
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.downloader.Downloader
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.coroutineMain
import com.flaco_music.utils.extentions.invoke
import com.flaco_music.utils.options.OptionsCallbackListener
import com.flaco_music.utils.options.OptionsUtils
import com.flaco_music.utils.options.OptionsUtils.OptionType
import com.flaco_music.utils.preferences.Preferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class TrackOptionsBottomSheetDialog(
    private val trackId: Int = -1,
    private val ownerId: Int = -1,
    private val playlistId: Int? = null,
    private val listener: OptionsCallbackListener? = null
) : BottomSheetDialogFragment(), KoinComponent {

    private var _binding: DialogOptionsBinding? = null
    private val binding get() = _binding!!

    private val audioRepository: AudioRepository by inject { parametersOf(false) }
    private val myMusicIdsRepository: MyMusicIdsRepository by inject()
    private val downloadsRepository: DownloadsRepository by inject()

    private val downloader: Downloader by inject()
    private val snackbarManager: SnackbarManager by inject()
    private val preferences: Preferences by inject()

    private val isTrackInUserAudios: Boolean
        get() = runBlocking(Dispatchers.IO) {
            track.decryptedUrl in myMusicIdsRepository.getMyMusicIds().map { it.url }
        }

    private val isTrackInDownloads: Boolean
        get() = runBlocking(Dispatchers.IO) {
            track.decryptedUrl in downloadsRepository.getDownloads().map { it.url }
        }

    private lateinit var track: Audio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioRepository.getById("${ownerId}_${trackId}").invoke(
            onResponseSuccessful = { response ->
                try {
                    response.body()?.response?.get(0)?.let {
                        track = it

                        val trackOptions = OptionsUtils(requireContext()).createPlaylistOptions(
                            *mutableListOf(
                                if (isTrackInUserAudios) {
                                    Pair(OptionType.REMOVE_FROM_MY_MUSIC_ID) { removeTrackFromAudios() }
                                } else {
                                    Pair(OptionType.ADD_TO_MY_MUSIC_ID) { addTrackToAudios() }
                                },
                                if (isTrackInDownloads) {
                                    Pair(OptionType.REMOVE_FROM_SAVED_ID) { removeTrackFromSaved() }
                                } else {
                                    Pair(OptionType.SAVE_ID) { saveTrack() }
                                },

                                Pair(OptionType.SHARE_ID) { shareTrack() },
                            ).apply {
                                if (playlistId == null) {
                                    add(2, Pair(OptionType.ADD_TO_PLAYLIST_ID) { addToPlaylist() })
                                } else {
                                    add(2, Pair(OptionType.REMOVE_FROM_PLAYLIST_ID) { removeFromPlaylist() })
                                }
                                if (track.lyricsId != null) {
                                    add(3, Pair(OptionType.LYRICS_ID) { showLyrics() })
                                }
                                if (track.album != null && track.mainArtists != null) {
                                    add(lastIndex, Pair(OptionType.VIEW_ALBUM_ID) { viewAlbum() })
                                    add(lastIndex, Pair(OptionType.VIEW_ARTIST_ID) { viewArtist() })
                                }
                            }.toTypedArray()
                        )

                        coroutineMain { binding.optionsRecycler.adapter = OptionsAdapter(trackOptions) }
                    }
                } catch (e: Exception) {
                }
            }
        )
    }

    private fun addTrackToAudios() {
        val call = audioRepository.addTrack(ownerId, trackId)
        call.invoke(
            onResponseSuccessful = {
                coroutineIO {
                    myMusicIdsRepository.insert(MyMusicIdItem(track.id, track.decryptedUrl))
                    listener?.onOptionSelected(OptionType.ADD_TO_MY_MUSIC_ID)

                    withContext(Dispatchers.Main) {
                        snackbarManager.showSnackbar(
                            getString(R.string.track_added),
                            SnackbarManager.SnackbarType.SUCCESS
                        )
                    }
                }
            },
            onResponseNotSuccessful = {
                Snackbar.make(requireParentFragment().requireView(), it.errorDescription, Snackbar.LENGTH_SHORT).show()
                snackbarManager.showSnackbar(getString(R.string.track_not_added), SnackbarManager.SnackbarType.ERROR)
            },
            anyway = {
                dismissAllowingStateLoss()
            }
        )
    }

    private fun removeTrackFromAudios() {
        val call = audioRepository.deleteTrack(ownerId, trackId)
        call.invoke(
            onResponseSuccessful = {
                coroutineIO {
                    myMusicIdsRepository.delete(trackId)
                    listener?.onOptionSelected(OptionType.REMOVE_FROM_MY_MUSIC_ID)
                    withContext(Dispatchers.Main) {
                        snackbarManager.showSnackbar(
                            getString(R.string.track_removed),
                            SnackbarManager.SnackbarType.SUCCESS
                        )
                    }
                }
            },
            onResponseNotSuccessful = {
                Snackbar.make(requireParentFragment().requireView(), it.errorDescription, Snackbar.LENGTH_SHORT).show()
                snackbarManager.showSnackbar(getString(R.string.track_not_removed), SnackbarManager.SnackbarType.ERROR)
            },
            anyway = { dismissAllowingStateLoss() }
        )
    }

    private fun saveTrack() {
        downloader.saveTrack(track)
        dismiss()
        listener?.onOptionSelected(OptionType.SAVE_ID)
    }

    private fun removeTrackFromSaved() {
        val trackId = downloadsRepository.getDownloads().find { it.url == track.decryptedUrl }?.trackId ?: -1
        downloader.delete(trackId)
        dismiss()
        listener?.onOptionSelected(OptionType.REMOVE_FROM_SAVED_ID)
    }

    private fun showLyrics() {
        val dialog = LyricsBottomSheetDialog(
            trackTitle = track.title,
            lyricsId = track.lyricsId!!
        )
        dialog.show(parentFragmentManager, LyricsBottomSheetDialog.TAG)
        dismiss()
        listener?.onOptionSelected(OptionType.LYRICS_ID)
    }

    private fun addToPlaylist() {
        val dialog = AddTrackToPlaylistBottomSheetDialog(
            trackId = trackId,
            ownerId = ownerId,
            accessKey = track.accessKey,
            onAdded = {
                snackbarManager.showSnackbar(
                    getString(R.string.track_added_to_playlist),
                    SnackbarManager.SnackbarType.SUCCESS
                )
                dismiss()
            },
            onError = {
                snackbarManager.showSnackbar(
                    getString(R.string.track_not_added_to_playlist),
                    SnackbarManager.SnackbarType.SUCCESS
                )
                dismiss()
            }
        )
        dialog.show(parentFragmentManager, AddTrackToPlaylistBottomSheetDialog.TAG)
        listener?.onOptionSelected(OptionType.ADD_TO_PLAYLIST_ID)
    }

    private fun removeFromPlaylist() {
        audioRepository.removeAudioFromPlaylist(
            playlistId = playlistId!!,
            ownerId = preferences.userId,
            audioIds = arrayOf("${ownerId}_${trackId}_${track.accessKey}")
        ).invoke(
            onResponseSuccessful = {
                snackbarManager.showSnackbar(
                    getString(R.string.track_removed_from_playlist),
                    SnackbarManager.SnackbarType.SUCCESS
                )

                coroutineMain {
                    delay(2000)
                    listener?.onOptionSelected(OptionType.REMOVE_FROM_PLAYLIST_ID)
                }
            },
            onResponseNotSuccessful = {
                snackbarManager.showSnackbar(
                    getString(R.string.track_not_removed_from_playlist),
                    SnackbarManager.SnackbarType.SUCCESS
                )
            },
            anyway = {
                dismiss()
            }
        )
    }

    private fun viewAlbum() {
        audioRepository.getAlbumsByArtist(track.mainArtists?.get(0)?.id ?: "").invoke(
            onResponseSuccessful = {
                coroutineMain {
                    val playlist = it.body()?.response?.items?.find { it.properPlaylistId == track.album?.id }
                    val arguments = Bundle().apply {
                        putInt("playlistId", playlist?.properPlaylistId ?: 0)
                        putInt("ownerId", playlist?.properOwnerId ?: 0)
                    }
                    requireActivity().findNavController(R.id.navHostFragment).navigate(R.id.playlistFragment, arguments)
                    listener?.onOptionSelected(OptionType.VIEW_ALBUM_ID)
                    dismiss()
                }
            }
        )
    }

    private fun viewArtist() {
        val arguments = Bundle().apply {
            putString("artistId", track.mainArtists?.get(0)?.id)
        }
        requireActivity().findNavController(R.id.navHostFragment).navigate(R.id.artistFragment, arguments)
        listener?.onOptionSelected(OptionType.VIEW_ARTIST_ID)
        dismiss()
    }

    private fun shareTrack() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "${track.artist} - ${track.title}")
            putExtra(Intent.EXTRA_TEXT, "https://flaco.music/track/${track.ownerId}_${track.id}")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, track.title)
        startActivity(shareIntent)
        dismiss()
        listener?.onOptionSelected(OptionType.SHARE_ID)
    }

    private fun showSleepTimer() {
        // TODO: implement sleep timer
    }

    companion object {
        const val TAG = "TrackOptionsBottomSheet"
    }
}