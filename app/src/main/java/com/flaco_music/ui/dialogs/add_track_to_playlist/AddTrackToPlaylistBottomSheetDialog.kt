package com.flaco_music.ui.dialogs.add_track_to_playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flaco_music.databinding.DialogAddTrackToPlaylistBinding
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.utils.extentions.coroutineMain
import com.flaco_music.utils.extentions.gone
import com.flaco_music.utils.extentions.invoke
import com.flaco_music.utils.extentions.visible
import com.flaco_music.utils.preferences.Preferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf

class AddTrackToPlaylistBottomSheetDialog(
    private val trackId: Int,
    private val ownerId: Int,
    private val accessKey: String,
    private val onAdded: () -> Unit,
    private val onError: () -> Unit
) : BottomSheetDialogFragment(), KoinComponent {

    private var _binding: DialogAddTrackToPlaylistBinding? = null
    private val binding get() = _binding!!

    private val audioRepository: AudioRepository by inject { parametersOf(false) }
    private val preferences: Preferences by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTrackToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioRepository.getPlaylists(preferences.userId).invoke(
            onResponseSuccessful = {
                val playlists = it.body()?.response?.items?.filter { it.permissions.edit } ?: emptyList()
                if (playlists.isNotEmpty()) {
                    coroutineMain { setupPlaylistsRecycler(playlists) }
                } else {
                    binding.playlistsRecycler.gone()
                    binding.noAlbumsText.visible()
                }
            }
        )
    }

    private fun setupPlaylistsRecycler(playlists: List<Playlist>) {
        binding.playlistsRecycler.adapter = OptionsPlaylistsAdapter(playlists) { playlistId ->
            addAudioToPlaylist(playlistId)
        }
    }

    private fun addAudioToPlaylist(playlistId: Int) {
        audioRepository.addAudioToPlaylist(
            playlistId = playlistId,
            ownerId = preferences.userId,
            audioIds = arrayOf("${ownerId}_${trackId}_${accessKey}")
        ).invoke(
            onResponseSuccessful = {
                onAdded()
            },
            onResponseNotSuccessful = {
                onError()
            },
            anyway = {
                dismiss()
            }
        )
    }

    companion object {
        const val TAG = "AddTrackToPlaylistBotto"
    }
}