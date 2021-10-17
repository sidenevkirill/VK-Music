package com.flaco_music.ui.screens.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.navigation.fragment.navArgs
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.databinding.FragmentHomeBinding
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.catalog.CatalogAudiosItem
import com.flaco_music.ui.adapters.catalog.CatalogItem
import com.flaco_music.ui.adapters.catalog.CatalogOthersItem
import com.flaco_music.ui.adapters.catalog.CatalogPlaylistsItem
import com.flaco_music.ui.screens.options.OptionsActivity
import com.flaco_music.ui.views.catalog.CatalogItemView
import com.flaco_music.ui.views.catalog.CatalogPlaylistsView
import com.flaco_music.ui.views.catalog.CatalogTracksView
import com.flaco_music.utils.extentions.goneWithFading
import com.flaco_music.utils.extentions.setupWithStatusBar
import com.flaco_music.utils.extentions.visibleWithFading
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel


class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentHomeBinding = { inflater, container ->
        FragmentHomeBinding.inflate(inflater, container, false)
    }

    private val viewModel: HomeViewModel by viewModel()

    private val navArgs: HomeFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadCatalog()
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

        val trackId = navArgs.trackId
        if (trackId.isNotEmpty()) {
            viewModel.deepLinker(trackId)
        }

        with(binding.swipeRefreshLayout) {
            setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN)
            setColorSchemeColors(Color.WHITE)
            setRefreshing(true)
            setOnRefreshListener {
                viewModel.loadCatalog()
            }
        }

        binding.downloadsImage.setOnClickListener {
            openDownloadsFragment()
        }

        binding.settingsImage.setOnClickListener {
            OptionsActivity.startMe(requireContext())
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), binding.swipeRefreshLayout)
    }

    private fun observeViewModel() {
        viewModel.catalogItemsLiveData.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                setupCatalog(it)
                initAds()
            }
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initAds() {
        MobileAds.initialize(requireContext()) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val adLoader: AdLoader =
            AdLoader.Builder(requireContext(), "ca-app-pub-2056309928986745/9108169147")
                .forNativeAd { ad ->
                    val styles = NativeTemplateStyle.Builder().build()
                    binding.nativeAd.setStyles(styles)
                    binding.nativeAd.setNativeAd(ad)
                    binding.nativeAd.visibleWithFading()
                }
                .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun setupCatalog(catalog: List<CatalogItem>) {
        binding.catalogContainer.children.forEach {
            if (it !is AdView && it !is TemplateView) {
                binding.catalogContainer.removeView(it)
            }
        }

        val tracks = catalog.filterIsInstance<CatalogAudiosItem>()
        var counter = 1

        try {
            catalog.forEachIndexed { index, item ->
                val catalogView = when (item) {
                    is CatalogAudiosItem -> CatalogTracksView(
                        context = requireContext(),
                        title = item.title,
                        viewPosition = tracks.indexOf(item),
                        lifecycleOwner = viewLifecycleOwner,
                        onTrackClicked = { position -> viewModel.play(position) },
                        onTrackLongClicked = { trackId, ownerId ->
                            openTrackOptionsBottomSheetDialog(
                                trackId,
                                ownerId
                            )
                        },
                        onShowAllClicked = { openCatalogTracksFragment(item.id) }
                    ).apply {
                        audios = item.audios
                    }
                    is CatalogPlaylistsItem -> CatalogPlaylistsView(
                        context = requireContext(),
                        title = item.title,
                        onPlaylistClicked = { playlistId, ownerId -> openPlaylistFragment(playlistId, ownerId) },
                        onPlaylistLongClicked = { playlistId, ownerId ->
                            openPlaylistOptionsBottomSheetDialog(
                                playlistId,
                                ownerId
                            )
                        },
                        onShowAllClicked = { openCatalogPlaylistsFragment(item.id) }
                    ).apply {
                        playlists = item.playlists
                    }
                    is CatalogOthersItem -> CatalogItemView(
                        requireContext(),
                        item.title,
                        onItemClicked = { id -> openItemFragment(id) },
                        onShowAllClicked = { openItemsFragment(item.id) }
                    ).apply {
                        items = item.items
                    }
                }

                if (index < 6) {
                    binding.catalogContainer.addView(catalogView, counter)
                    counter++
                } else {
                    binding.catalogContainer.addView(catalogView)
                }
            }
        } catch (e: IllegalStateException) {
            // do nothing
        }
    }

    private fun openCatalogTracksFragment(sectionId: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToCatalogTracksFragment(sectionId)
        navigateTo(action)
    }

    private fun openCatalogPlaylistsFragment(sectionId: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToCatalogPlaylistsFragment(sectionId)
        navigateTo(action)
    }

    private fun openItemsFragment(sectionId: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToCatalogItemsFragment(sectionId)
        navigateTo(action)
    }

    private fun openPlaylistFragment(playlistId: Int, ownerId: Int) {
        val action = HomeFragmentDirections.actionHomeFragmentToPlaylistFragment(playlistId, ownerId)
        navigateTo(action)
    }

    private fun openItemFragment(itemId: Int) {
        val action = HomeFragmentDirections.actionHomeFragmentToFriendMusicFragment(itemId)
        navigateTo(action)
    }

    private fun openDownloadsFragment() {
        val action = HomeFragmentDirections.actionHomeFragmentToDownloadsFragment()
        navigateTo(action)
    }

    override fun showLoading() {
        binding.swipeRefreshLayout.setRefreshing(true)
        if (isFirstLaunch) {
            binding.scrollView.goneWithFading()
        }
    }

    override fun hideLoading() {
        binding.swipeRefreshLayout.setRefreshing(false)
        if (isFirstLaunch) {
            binding.scrollView.visibleWithFading()
            isFirstLaunch = false
        }
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}