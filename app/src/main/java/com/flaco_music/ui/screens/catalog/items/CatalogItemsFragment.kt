package com.flaco_music.ui.screens.catalog.items

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.databinding.FragmentCatalogItemsBinding
import com.flaco_music.retrofit.models.CatalogSectionItem
import com.flaco_music.ui.BaseFragment
import com.flaco_music.ui.adapters.catalog.items.CatalogItemsSectionAdapter
import com.flaco_music.utils.extentions.goneWithFading
import com.flaco_music.utils.extentions.setupWithStatusBar
import com.flaco_music.utils.extentions.visibleWithFading
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class CatalogItemsFragment : BaseFragment<FragmentCatalogItemsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FragmentCatalogItemsBinding = { inflater, container ->
        FragmentCatalogItemsBinding.inflate(inflater, container, false)
    }

    private val viewModel: CatalogItemsViewModel by viewModel()

    private val navArgs: CatalogItemsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadItems(navArgs.sectionId)
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
                viewModel.loadItems(navArgs.sectionId)
            }
        }

        binding.appBarLayout.setupWithStatusBar(requireActivity(), binding.swipeRefreshLayout)
    }

    private fun observeViewModel() {
        viewModel.titleListData.observe(viewLifecycleOwner) {
            binding.title.text = it
        }
        viewModel.itemsLiveData.observe(viewLifecycleOwner) {
            GlobalScope.launch(Dispatchers.Main) {
                binding.swipeRefreshLayout.setRefreshing(false)
                delay(400)
                setupItemsRecycler(it)
            }
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) {
            if (it) showLoading() else hideLoading()
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupItemsRecycler(items: List<CatalogSectionItem>) {
        val adapter = CatalogItemsSectionAdapter(items) { ownerId ->
            openItemFragment(ownerId)
        }
        binding.itemsRecycler.adapter = adapter
    }

    private fun openItemFragment(ownerId: Int) {
        val action = CatalogItemsFragmentDirections.actionCatalogItemsFragmentToFriendMusicFragment(ownerId)
        navigateTo(action)
    }

    override fun showLoading() {
        binding.swipeRefreshLayout.setRefreshing(true)
        if (isFirstLaunch) {
            binding.itemsRecycler.goneWithFading()
        }
    }

    override fun hideLoading() {
        binding.swipeRefreshLayout.setRefreshing(false)
        if (isFirstLaunch) {
            binding.itemsRecycler.visibleWithFading()
            isFirstLaunch = false
        }
    }
}