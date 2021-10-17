package com.flaco_music.ui.dialogs.artist_options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flaco_music.databinding.DialogOptionsBinding
import com.flaco_music.retrofit.models.Artist
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.ui.adapters.options.OptionsAdapter
import com.flaco_music.utils.extentions.invoke
import com.flaco_music.utils.options.OptionsUtils
import com.flaco_music.utils.options.OptionsUtils.OptionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class ArtistOptionsBottomSheetDialog(
    private val artistId: String,
) : BottomSheetDialogFragment(), KoinComponent {

    private var _binding: DialogOptionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArtistOptionsViewModel by viewModel()

    private val audioRepository: AudioRepository by inject { parametersOf(false) }

    private lateinit var artist: Artist

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioRepository.getArtist(artistId).invoke(
            onResponseSuccessful = { response ->
                response.body()?.response?.artists?.get(0)?.let {
                    artist = it

                    val artistOptions = OptionsUtils(requireContext()).createPlaylistOptions(
                        Pair(OptionType.SHUFFLE_PLAY) { shufflePlay() },
                        Pair(OptionType.SHARE_ID) { shareArtist() }
                    )

                    GlobalScope.launch(Dispatchers.Main) {
                        binding.optionsRecycler.adapter = OptionsAdapter(artistOptions)
                    }
                }
            }
        )
    }

    private fun shufflePlay() {
        viewModel.shufflePlay(artistId)
        dismiss()
    }

    private fun shareArtist() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, artist.name)
            putExtra(Intent.EXTRA_TEXT, "https://flaco.music/artist/$artistId")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, artist.name)
        startActivity(shareIntent)
        dismiss()
    }

    companion object {
        const val TAG = "TrackOptionsBottomSheet"
    }
}