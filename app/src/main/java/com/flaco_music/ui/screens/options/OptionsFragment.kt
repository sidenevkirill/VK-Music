package com.flaco_music.ui.screens.options

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.flaco_music.BuildConfig
import com.flaco_music.R
import com.flaco_music.databinding.FragmentOptionsBinding
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.screens.login.LoginActivity
import com.flaco_music.utils.AlertDialogManager
import com.flaco_music.utils.CoverResolutionHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent


class OptionsFragment : BaseFragment<FragmentOptionsBinding>(), KoinComponent {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentOptionsBinding = { inflater, container ->
        FragmentOptionsBinding.inflate(inflater, container, false)
    }

    private val viewModel: OptionsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        observeViewModel()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(requireContext()) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val alertDialogManager = AlertDialogManager(requireActivity())

        binding.coverResolutionFrame.setOnClickListener {
            openCoverResolutionDialog()
        }

        binding.sendFeedbackFrame.setOnClickListener {
            openFeedbackFragment()
        }

        binding.tellFriendsFrame.setOnClickListener {
            tellFriends()
        }

        binding.contactDeveloperFrame.setOnClickListener {
            contactDeveloper()
        }

        binding.darkModeFrame.setOnClickListener {
            viewModel.onEnableDarkModeSwitchChecked(!binding.darkModeSwitch.isChecked)
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEnableDarkModeSwitchChecked(isChecked)

            val darkMode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            AppCompatDelegate.setDefaultNightMode(darkMode)
        }

        binding.enableAutoCachingFrame.setOnClickListener {
            viewModel.onEnableAutoCachingSwitchChecked(!binding.enableAutoCachingSwitch.isChecked)
        }

        binding.enableAutoCachingSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEnableAutoCachingSwitchChecked(isChecked)
        }

        binding.logoutFrame.setOnClickListener {
            alertDialogManager.showDialog(
                title = getString(R.string.log_out),
                message = getString(R.string.logout_description),
                iconResId = R.drawable.ic_baseline_logout_24,
                positiveButtonText = getString(R.string.yes),
                onPositiveButtonClicked = {
                    viewModel.logout()
                    LoginActivity.startMe(requireContext())
                    requireActivity().finish()
                },
                negativeButtonText = getString(R.string.cancel)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val nightModeFlags = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
            requireActivity().window.apply {
                decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                statusBarColor = ContextCompat.getColor(requireContext(), R.color.background)
            }
        } else {
            requireActivity().window.apply {
                decorView.systemUiVisibility =
                    decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                statusBarColor = ContextCompat.getColor(requireContext(), R.color.background)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.apply {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
    }

    private fun observeViewModel() {
        viewModel.isDarkModeEnabledLiveData.observe(viewLifecycleOwner) {
            binding.darkModeSwitch.isChecked = it
        }
        viewModel.isAutoCachingEnabledLiveData.observe(viewLifecycleOwner) {
            binding.enableAutoCachingSwitch.isChecked = it
        }
    }

    @SuppressLint("CheckResult")
    private fun openCoverResolutionDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.cover_resolution)
            listItemsSingleChoice(
                items = CoverResolutionHelper.coverResolutions,
                initialSelection = CoverResolutionHelper.coverResolutionIndex
            ) { _, index, _ ->
                viewModel.changeCoverResolution(index)
            }
        }
    }

    private fun openFeedbackFragment() {
        navigateTo(OptionsFragmentDirections.actionOptionsFragmentToFeedbackFragment())
    }

    private fun tellFriends() {
        val link = "https://play.google.com/store/apps/details?id=${requireActivity().packageName}"
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.tell_friends_message, link))
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun contactDeveloper() {
        val mailIntent: Intent = Intent().apply {
            action = Intent.ACTION_SENDTO
            data = Uri.parse("mailto:" + BuildConfig.DEVELOPER_EMAIL)
        }

        val contactDeveloperIntent = Intent.createChooser(mailIntent, null)
        startActivity(contactDeveloperIntent)
    }
}