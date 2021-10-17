package com.flaco_music.ui.screens.genre

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.R
import com.flaco_music.databinding.FragmentGenreBinding
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.tracks.TracksAdapter
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.extentions.goneWithFading
import com.flaco_music.utils.extentions.setupWithStatusBar
import com.flaco_music.utils.extentions.visibleWithFading
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class GenreFragment : BaseFragment<FragmentGenreBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentGenreBinding = { inflater, container ->
        FragmentGenreBinding.inflate(inflater, container, false)
    }

    private val viewModel: GenreViewModel by inject()

    private val navArgs: GenreFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadTracks(navArgs.genreId, navArgs.genreName)
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

        with(binding.swipeRefreshLayout) {
            setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN)
            setColorSchemeColors(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)
            setRefreshing(true)
            setOnRefreshListener {
                viewModel.loadTracks(navArgs.genreId, navArgs.genreName)
            }
        }

        binding.appBarBackground.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(navArgs.color, ContextCompat.getColor(requireContext(), R.color.background))
        )

        binding.swipeRefreshLayout.backgroundTintList = ColorStateList.valueOf(navArgs.color)

        binding.appBarLayout.setupWithStatusBar(requireActivity(), binding.swipeRefreshLayout, navArgs.color)
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).setBackgroundColor(navArgs.color)
        requireActivity().window.apply {
            decorView.systemUiVisibility =
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            statusBarColor = navArgs.color
        }
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary
            )
        )
        requireActivity().window.apply {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
    }

    private fun observeViewModel() {
        viewModel.genreNameLiveData.observe(viewLifecycleOwner) {
            binding.playlistNameText.text = navArgs.genreName
        }
        viewModel.tracksLiveData.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                binding.swipeRefreshLayout.setRefreshing(false)
                delay(400)
                setupTracksRecycler(it)
            }
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupTracksRecycler(tracks: List<Audio>) {
        binding.tracksRecycler.adapter = TracksAdapter(
            tracks = tracks,
            lifecycleOwner = try {
                viewLifecycleOwner
            } catch (e: Exception) {
                return
            },
            onClick = { position -> viewModel.play(position) },
            onLongClick = { trackId, ownerId -> openTrackOptionsBottomSheetDialog(trackId, ownerId) }
        )
    }

    override fun showLoading() {
        binding.swipeRefreshLayout.setRefreshing(true)
        if (isFirstLaunch) {
            binding.tracksRecycler.goneWithFading()
        }
    }

    override fun hideLoading() {
        binding.swipeRefreshLayout.setRefreshing(false)
        if (isFirstLaunch) {
            binding.tracksRecycler.visibleWithFading()
            isFirstLaunch = false
        }
    }
}