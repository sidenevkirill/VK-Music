package com.flaco_music.ui.adapters.catalog.playlists

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.flaco_music.R
import com.flaco_music.databinding.ItemCatalogSectionPlaylistBinding
import com.flaco_music.retrofit.models.CatalogSectionPlaylist
import com.flaco_music.utils.extentions.gone
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.extentions.onLongClick

class CatalogPlaylistsSectionAdapter(
    private val playlists: List<CatalogSectionPlaylist>,
    private val onClick: (playlistId: Int, ownerId: Int) -> Unit,
    private val onLongClick: (playlistId: Int, ownerId: Int) -> Unit
) : RecyclerView.Adapter<CatalogPlaylistsSectionAdapter.CatalogPlaylistsSectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogPlaylistsSectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_catalog_section_playlist,
            parent,
            false
        )

        val binding = ItemCatalogSectionPlaylistBinding.bind(view)
        return CatalogPlaylistsSectionViewHolder(binding)
    }

    override fun getItemCount(): Int = playlists.size

    override fun onBindViewHolder(holder: CatalogPlaylistsSectionViewHolder, position: Int) {
        val track = playlists[position]
        holder.bind(track)
    }

    inner class CatalogPlaylistsSectionViewHolder(private val binding: ItemCatalogSectionPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: CatalogSectionPlaylist) {
            binding.nameText.text = playlist.title
            binding.artistNameText.text = playlist.mainArtists?.let { artists ->
                TextUtils.join(", ", artists.map { it.name })
            } ?: run {
                binding.artistNameText.gone()
                null
            }

            loadPlaylistCover(playlist.photo?.photo300)

            itemView.onClick { onClick(playlist.id, playlist.ownerId) }
            itemView.onLongClick { onLongClick(playlist.id, playlist.ownerId) }
        }

        private fun loadPlaylistCover(coverUrl: String?) {
            val builder: ImageRequest.Builder.() -> Unit = {
                crossfade(CatalogPlaylistsAdapter.CROSSFADE)
                transformations(RoundedCornersTransformation(CatalogPlaylistsAdapter.CORNER_RADIUS))
                diskCachePolicy(CachePolicy.ENABLED)
            }

            coverUrl?.let {
                binding.coverImage.load(uri = coverUrl, builder = builder)
            } ?: binding.coverImage.load(drawableResId = R.drawable.playlist_placeholder_medium, builder = builder)
        }
    }
}