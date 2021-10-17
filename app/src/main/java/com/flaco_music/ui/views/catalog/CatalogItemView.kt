package com.flaco_music.ui.views.catalog

import android.content.Context
import android.view.LayoutInflater
import com.flaco_music.databinding.ViewCatalogItemBinding
import com.flaco_music.retrofit.models.CatalogSectionItem
import com.flaco_music.ui.adapters.catalog.items.CatalogItemsAdapter
import com.flaco_music.utils.extentions.onClick

class CatalogItemView(
    context: Context,
    override var title: String,
    private val onItemClicked: (id: Int) -> Unit,
    override val onShowAllClicked: () -> Unit
) : CatalogView(context) {

    var items: List<CatalogSectionItem>? = null
        set(value) {
            field = value
            value?.let { setupListAdapter(it) }
        }

    override val binding: ViewCatalogItemBinding = ViewCatalogItemBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    ).also {
        removeAllViews()
        addView(it.root)

        it.titleText.text = title
        it.showAllText.onClick {
            onShowAllClicked()
        }
    }

    private fun setupListAdapter(tracks: List<CatalogSectionItem>) {
        val adapter = CatalogItemsAdapter(tracks) {
            onItemClicked(it)
        }
        binding.itemsRecycler.adapter = adapter
    }

    companion object {
        private const val TAG = "CatalogOthersMusicView"
    }
}