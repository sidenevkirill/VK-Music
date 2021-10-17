package com.flaco_music.ui.dialogs.lyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flaco_music.databinding.DialogLyricsBinding
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.utils.extentions.coroutineMain
import com.flaco_music.utils.extentions.invoke
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class LyricsBottomSheetDialog(private val trackTitle: String, private val lyricsId: Int) :
    BottomSheetDialogFragment(), KoinComponent {

    private var _binding: DialogLyricsBinding? = null
    private val binding get() = _binding!!

    private val audioRepository: AudioRepository by inject { parametersOf(false) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLyricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioRepository.getLyrics(lyricsId).invoke(
            onResponseSuccessful = { response ->
                coroutineMain {
                    binding.trackNameText.text = trackTitle
                    binding.lyricsText.text = response.body()?.response?.text ?: ""
                }
            }
        )
    }

    companion object {
        const val TAG = "LyricsBottomSheetDialog"
    }
}