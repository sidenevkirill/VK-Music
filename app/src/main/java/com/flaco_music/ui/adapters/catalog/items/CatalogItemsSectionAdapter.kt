package com.flaco_music.ui.adapters.catalog.items

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.flaco_music.R
import com.flaco_music.databinding.ItemsCatalogSectionItemBinding
import com.flaco_music.retrofit.models.CatalogSectionItem
import com.flaco_music.ui.adapters.catalog.playlists.CatalogPlaylistsAdapter
import com.flaco_music.utils.extentions.onClick

class CatalogItemsSectionAdapter(
    private val items: List<CatalogSectionItem>,
    private val callback: (ownerId: Int) -> Unit
) : RecyclerView.Adapter<CatalogItemsSectionAdapter.CatalogItemsSectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogItemsSectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.items_catalog_section_item,
            parent,
            false
        )

        val binding = ItemsCatalogSectionItemBinding.bind(view)
        return CatalogItemsSectionViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CatalogItemsSectionViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class CatalogItemsSectionViewHolder(private val binding: ItemsCatalogSectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CatalogSectionItem) {
            binding.name.text = item.title

            val coverImage = try {
                item.image?.get(0)?.url
            } catch (e: IndexOutOfBoundsException) {
                null
            }

            loadPlaylistCover(coverImage)

            val itemId = TextUtils.join(
                "",
                "-*\\d+".toRegex().findAll(item.url).toList().map { it.value }
            ).toInt()

            itemView.onClick { callback(itemId) }
        }

        private fun loadPlaylistCover(coverUrl: String?) {
            val builder: ImageRequest.Builder.() -> Unit = {
                crossfade(CatalogPlaylistsAdapter.CROSSFADE)
                transformations(RoundedCornersTransformation(CatalogPlaylistsAdapter.CORNER_RADIUS))
                diskCachePolicy(CachePolicy.ENABLED)
            }

            coverUrl?.let {
                binding.avatarImage.load(uri = coverUrl, builder = builder)
            } ?: binding.avatarImage.load(drawableResId = R.drawable.artist_placeholder_medium, builder = builder)
        }
    }

    companion object {
        private const val TAG = "CatalogItemsSectionAdap"
    }
}