package com.flaco_music.ui.screens.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flaco_music.databinding.FragmentExploreBinding
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.genres.GenresAdapter
import com.flaco_music.utils.constants.ApiConstants
import com.flaco_music.utils.extentions.gone
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.extentions.setupWithStatusBar
import com.flaco_music.utils.extentions.visible
import org.koin.android.viewmodel.ext.android.viewModel

class ExploreFragment : BaseFragment<FragmentExploreBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentExploreBinding = { inflater, container ->
        FragmentExploreBinding.inflate(inflater, container, false)
    }

    private val viewModel: ExploreViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadGenres()
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

        binding.searchContainer.onClick {
            openSearchFragment()
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity())
    }

    private fun observeViewModel() {
        viewModel.genresLiveData.observe(viewLifecycleOwner) {
            setupGenresRecycler(it)
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showProgressBar() else hideProgressBar()
        }
    }

    private fun setupGenresRecycler(genres: List<ApiConstants.Genres.Genre>) {
        val adapter = GenresAdapter(genres) { genreId, genreName, color ->
            val action = ExploreFragmentDirections.actionSearchFragmentToGenreFragment(genreId, genreName, color)
            navigateTo(action)
        }
        binding.genresRecycler.adapter = adapter
    }

    private fun showProgressBar() {
        binding.appBarLayout.gone()
        binding.genresRecycler.gone()
        binding.progressBar.visible()
    }

    private fun hideProgressBar() {
        binding.appBarLayout.visible()
        binding.genresRecycler.visible()
        binding.progressBar.gone()
    }

    private fun openSearchFragment() {
        val action = ExploreFragmentDirections.actionExploreFragmentToSearchFragment()
        navigateTo(action)
    }

    companion object {
        const val TAG = "ExploreFragment"
    }
}