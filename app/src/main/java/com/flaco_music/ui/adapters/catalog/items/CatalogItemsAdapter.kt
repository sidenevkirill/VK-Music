package com.flaco_music.ui.adapters.catalog.items

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.flaco_music.R
import com.flaco_music.databinding.ItemCatalogItemBinding
import com.flaco_music.retrofit.models.CatalogSectionItem
import com.flaco_music.utils.extentions.onClick

class CatalogItemsAdapter(
    private val items: List<CatalogSectionItem>,
    private val callback: (itemId: Int) -> Unit
) : RecyclerView.Adapter<CatalogItemsAdapter.CatalogItemsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogItemsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_catalog_item, parent, false)
        val binding = ItemCatalogItemBinding.bind(view)
        return CatalogItemsViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CatalogItemsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class CatalogItemsViewHolder(private val binding: ItemCatalogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CatalogSectionItem) {
            binding.name.text = item.title

            val trackCover = try {
                item.image?.get(0)?.url
            } catch (e: IndexOutOfBoundsException) {
                null
            }

            loadTrackCover(trackCover)

            itemView.onClick {
                val itemId = TextUtils.join(
                    "",
                    "-*\\d+".toRegex().findAll(item.url).toList().map { it.value }
                ).toInt()

                callback(itemId)
            }
        }

        private fun loadTrackCover(coverUrl: String?) {
            val builder: ImageRequest.Builder.() -> Unit = {
                crossfade(CROSSFADE)
                transformations(CircleCropTransformation())
                diskCachePolicy(CachePolicy.ENABLED)
            }

            coverUrl?.let {
                binding.avatarImage.load(uri = coverUrl, builder = builder)
            } ?: binding.avatarImage.load(drawableResId = R.drawable.artist_placeholder_medium, builder = builder)
        }
    }

    companion object {
        private const val TAG = "CatalogItemsAdapter"
        const val CROSSFADE = true
    }
}