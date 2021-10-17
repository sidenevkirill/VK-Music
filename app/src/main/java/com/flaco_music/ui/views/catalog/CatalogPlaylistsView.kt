package com.flaco_music.ui.views.catalog

import android.content.Context
import android.view.LayoutInflater
import com.flaco_music.databinding.ViewCatalogPlaylistsBinding
import com.flaco_music.retrofit.models.CatalogSectionPlaylist
import com.flaco_music.ui.adapters.catalog.playlists.CatalogPlaylistsAdapter
import com.flaco_music.utils.extentions.onClick

class CatalogPlaylistsView(
    context: Context,
    override var title: String,
    private val onPlaylistClicked: (playlistId: Int, ownerId: Int) -> Unit,
    private val onPlaylistLongClicked: (playlistId: Int, ownerId: Int) -> Unit,
    override val onShowAllClicked: () -> Unit
) : CatalogView(context) {

    var playlists: List<CatalogSectionPlaylist>? = null
        set(value) {
            field = value
            value?.let { setupListAdapter(it) }
        }

    override val binding: ViewCatalogPlaylistsBinding = ViewCatalogPlaylistsBinding.inflate(
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

    private fun setupListAdapter(playlists: List<CatalogSectionPlaylist>) {
        binding.playlistsRecycler.adapter = CatalogPlaylistsAdapter(
            playlists = playlists,
            onClick = { playlistId, ownerId ->
                onPlaylistClicked(playlistId, ownerId)
            },
            onLongClick = { playlistId, ownerId ->
                onPlaylistLongClicked(playlistId, ownerId)
            }
        )
    }

    companion object {
        private const val TAG = "CatalogPlaylistsView"
    }
}