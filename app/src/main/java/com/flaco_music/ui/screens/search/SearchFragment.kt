package com.flaco_music.ui.screens.search

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.R
import com.flaco_music.databinding.FragmentSearchBinding
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.search.*
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.extentions.hideKeyboard
import com.flaco_music.utils.extentions.showKeyboard
import com.flaco_music.utils.extentions.string
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentSearchBinding = { inflater, container ->
        FragmentSearchBinding.inflate(inflater, container, false)
    }

    private val viewModel: SearchViewModel by viewModel()

    private val searchFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        with(binding.root) {
            if (hasFocus) showKeyboard() else hideKeyboard()
        }
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

        viewModel.loadSearchHistory()

        setupSearchInput()

        binding.swipeRefreshLayout.isEnabled = false

        with(binding.swipeRefreshLayout) {
            setRefreshStyle(PullRefreshLayout.STYLE_RING)
            setOnRefreshListener {
                viewModel.search(binding.searchInput.string, false)
            }
        }

        binding.motionLayout.postOnAnimation {
            binding.searchInput.requestFocus()
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
        requireView().hideKeyboard()
        requireActivity().window.apply {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
    }

    private fun observeViewModel() {
        viewModel.searchItemsLiveData.observe(viewLifecycleOwner) {
            setupSearchRecycler(it)
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            requireView().hideKeyboard()
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupSearchInput() {
        with(binding.searchInput) {
            onFocusChangeListener = searchFocusChangeListener
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // do nothing
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // do nothing
                }

                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString()
                    if (query.isNotBlank()) {
                        viewModel.search(query, true)
                    }
                }
            })
        }
    }

    private fun setupSearchRecycler(items: List<SearchItem>) {
        binding.tracksRecycler.adapter = SearchAdapter(
            items = items,
            lifecycleOwner = viewLifecycleOwner,
            onClick = { searchItem, position ->
                when (searchItem) {
                    is Audio -> viewModel.play(position)
                    is Album -> openPlaylistFragment(searchItem.id, searchItem.ownerId)
                    is Artist -> openArtistFragment(searchItem.id)
                }
                viewModel.saveToSearchHistory(searchItem)
            },
            onLongClick = { searchItem ->
                requireView().hideKeyboard()
                when (searchItem) {
                    is Audio -> (requireActivity() as MainActivity).openTrackOptionsBottomSheetDialog(
                        searchItem.id,
                        searchItem.ownerId
                    )
                    is Album -> (requireActivity() as MainActivity).openPlaylistOptionsBottomSheetDialog(
                        searchItem.id,
                        searchItem.ownerId
                    )
                    is Artist -> (requireActivity() as MainActivity).openArtistOptionsBottomSheetDialog(
                        searchItem.id
                    )
                }
            }
        )
    }

    private fun openPlaylistFragment(playlistId: Int, ownerId: Int) {
        val action = SearchFragmentDirections.actionSearchFragmentToPlaylistFragment(playlistId, ownerId)
        navigateTo(action)
    }

    private fun openArtistFragment(artistId: String) {
        val action = SearchFragmentDirections.actionSearchFragmentToArtistFragment(artistId)
        navigateTo(action)
    }

    override fun showLoading() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.swipeRefreshLayout.setRefreshing(true)
            delay(400)
        }
    }

    override fun hideLoading() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.swipeRefreshLayout.setRefreshing(false)
            delay(400)
        }
    }

    companion object {
        private const val TAG = "SearchFragment"
    }
}