package com.flaco_music.ui.adapters.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.flaco_music.R
import com.flaco_music.databinding.SearchItemBinding
import com.flaco_music.databinding.ViewNativeAdBinding
import com.flaco_music.db.current_playlist.CurrentPlaylistRepository
import com.flaco_music.db.downloads.DownloadItem
import com.flaco_music.db.downloads.DownloadsRepository
import com.flaco_music.utils.extentions.gone
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.extentions.onLongClick
import com.flaco_music.utils.extentions.visible
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchAdapter(
    private val items: List<SearchItem>,
    private val lifecycleOwner: LifecycleOwner,
    private val onClick: (searchItem: SearchItem, position: Int) -> Unit,
    private val onLongClick: (searchItem: SearchItem) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>(), KoinComponent {

    private var currentPlayingTrackUrl: String? = null

    private var adCounter = 0

    private var downloads: List<DownloadItem>? = null

    private val adsIds = listOf(
        "ca-app-pub-2056309928986745/7323194626",
        "ca-app-pub-2056309928986745/9047339836",
        "ca-app-pub-2056309928986745/3383949617",
        "ca-app-pub-2056309928986745/5462839113",
        "ca-app-pub-2056309928986745/3795013151"
    )

    private val currentPlaylistRepository: CurrentPlaylistRepository by inject()
    private val downloadsRepository: DownloadsRepository by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        observeCurrentPlayingTrack()
        observeDownloads()
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        val binding = SearchItemBinding.bind(view)
        return SearchViewHolder(binding, viewType == VIEW_TYPE_AD)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 1
            && (position + 1) % AD_PERIODIZATION == 0
            && (position + 1) <= (AD_PERIODIZATION * adsIds.size)
        ) {
            VIEW_TYPE_AD
        } else {
            VIEW_TYPE_TRACK
        }
    }

    private fun observeCurrentPlayingTrack() {
        currentPlaylistRepository.currentPlayingTrackLiveData.observe(lifecycleOwner) {
            if (it?.url != currentPlayingTrackUrl) {
                currentPlayingTrackUrl = it?.url
                this@SearchAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun observeDownloads() {
        downloadsRepository.getDownloadsLiveData().observe(lifecycleOwner) {
            if (it != downloads) {
                downloads = it
                this@SearchAdapter.notifyDataSetChanged()
            }
        }
    }

    inner class SearchViewHolder(private val binding: SearchItemBinding, private val isWithAd: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(searchItem: SearchItem) {
            val title = searchItem.title
            val coverUrl = searchItem.coverUrl
            val placeholderImageId = when (searchItem) {
                is Audio -> R.drawable.track_placeholder_small
                is Album -> R.drawable.playlist_placeholder_small
                is Artist -> R.drawable.artist_placeholder_small
            }

            val subtitle = when (searchItem) {
                is Audio -> String.format(itemView.context.getString(R.string.search_audio_item), searchItem.artist)
                is Album -> String.format(itemView.context.getString(R.string.search_album_item), searchItem.artist)
                is Artist -> itemView.context.getString(R.string.search_artist_item)
            }

            val hasCircleImage = when (searchItem) {
                is Artist -> true
                else -> false
            }

            binding.title.text = title
            binding.subtitle.text = subtitle

            binding.root.onClick {
                onClick(searchItem, items.filter { it is Audio }.indexOf(searchItem))
            }

            binding.root.onLongClick {
                onLongClick(searchItem)
            }

            loadCover(coverUrl, placeholderImageId, hasCircleImage)

            if (searchItem is Audio) {
                val nameTextColorId: Int = if (searchItem.url == currentPlayingTrackUrl) {
                    R.color.primary
                } else {
                    R.color.primary_text
                }

                binding.title.setTextColor(ContextCompat.getColor(itemView.context, nameTextColorId))

                val minutes = searchItem.duration / 60
                val seconds = searchItem.duration - (60 * minutes)

                binding.durationText.visible()
                binding.durationText.text = "$minutes:${if (seconds in 0..9) "0$seconds" else seconds}"

                if (downloadsRepository.checkDoesTrackExist(searchItem.url)) {
                    binding.isDownloadedIndicatorImage.visible()
                } else {
                    binding.isDownloadedIndicatorImage.gone()
                }
            } else {
                binding.durationText.gone()
                binding.isDownloadedIndicatorImage.gone()
            }

            if (isWithAd) {
                binding.root.children.forEach {
                    if (it is TemplateView) {
                        binding.root.removeView(it)
                    }
                }

                val view =
                    LayoutInflater.from(binding.root.context).inflate(R.layout.view_native_ad, binding.root, false)
                val adBinding = ViewNativeAdBinding.bind(view)

                val adLoader: AdLoader =
                    AdLoader.Builder(adBinding.root.context, adsIds[adCounter])
                        .forNativeAd { ad ->
                            val styles = NativeTemplateStyle.Builder().build()
                            adBinding.nativeAd.setStyles(styles)
                            adBinding.nativeAd.setNativeAd(ad)
                        }
                        .build()

                adLoader.loadAd(AdRequest.Builder().build())

                binding.root.addView(view)

                adCounter++

                if (adCounter == adsIds.size) {
                    adCounter = 0
                }
            }
        }

        private fun loadCover(coverUrl: String?, placeholderImageId: Int, hasCircleImage: Boolean) {
            val builder: ImageRequest.Builder.() -> Unit = {
                crossfade(CROSSFADE)
                diskCachePolicy(CachePolicy.ENABLED)
                if (hasCircleImage) {
                    transformations(CircleCropTransformation())
                }
            }

            coverUrl?.let {
                binding.coverImage.load(uri = coverUrl, builder = builder)
            } ?: binding.coverImage.load(drawableResId = placeholderImageId, builder = builder)
        }
    }

    companion object {
        const val CROSSFADE = true
        private const val AD_PERIODIZATION = 12
        private const val VIEW_TYPE_AD = 100
        private const val VIEW_TYPE_TRACK = 101
    }
}