package com.flaco_music.ui.screens.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flaco_music.R
import com.flaco_music.databinding.FragmentFeedbackBinding
import com.flaco_music.ui.BaseFragment
import com.flaco_music.utils.extentions.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import org.koin.android.viewmodel.ext.android.viewModel

class FeedbackFragment : BaseFragment<FragmentFeedbackBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentFeedbackBinding = { inflater, container ->
        FragmentFeedbackBinding.inflate(inflater, container, false)
    }

    private val viewModel: FeedbackViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendFab.setOnClickListener {
            sendFeedback()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)!!
    }

    private fun sendFeedback() {
        val message = binding.messageInput.text.toString()
        if (message.isNotBlank()) {
            viewModel.sendFeedback(message)
            requireActivity().onBackPressed()

            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                getString(R.string.message_sent),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            binding.messageInput.error = getString(R.string.error_feedback_empty)
        }
    }

    override fun onDestroyView() {
        requireView().hideKeyboard()
        super.onDestroyView()
    }
}