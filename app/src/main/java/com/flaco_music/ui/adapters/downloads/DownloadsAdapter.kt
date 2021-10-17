package com.flaco_music.ui.adapters.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.flaco_music.R
import com.flaco_music.databinding.ItemTrackBinding
import com.flaco_music.databinding.ViewNativeAdBinding
import com.flaco_music.db.current_playlist.CurrentPlaylistRepository
import com.flaco_music.db.downloads.DownloadItem
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.extentions.onLongClick
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloadsAdapter(
    private val tracks: List<DownloadItem>,
    private val lifecycleOwner: LifecycleOwner,
    private val onClick: (position: Int) -> Unit,
    private val onLongClick: (trackId: Int, ownerId: Int) -> Unit
) : RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder>(), KoinComponent {

    private var currentPlayingTrackUrl: String? = null

    private val currentPlaylistRepository: CurrentPlaylistRepository by inject()

    private var adCounter = 0

    private val adsIds = listOf(
        "ca-app-pub-2056309928986745/1230034348",
        "ca-app-pub-2056309928986745/7603871003",
        "ca-app-pub-2056309928986745/8953693263",
        "ca-app-pub-2056309928986745/9948044888",
        "ca-app-pub-2056309928986745/7412299317"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsViewHolder {
        observeCurrentPlayingTrack()
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        val binding = ItemTrackBinding.bind(view)
        return DownloadsViewHolder(binding, viewType == VIEW_TYPE_AD)
    }

    override fun getItemCount(): Int = tracks.size

    override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) {
        val track = tracks[position]
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

    override fun onViewDetachedFromWindow(holder: DownloadsViewHolder) {
        holder.itemView.clearAnimation()
    }

    private fun observeCurrentPlayingTrack() {
        currentPlaylistRepository.currentPlayingTrackLiveData.observe(lifecycleOwner) {
            if (it?.url != currentPlayingTrackUrl) {
                currentPlayingTrackUrl = it?.url
                this@DownloadsAdapter.notifyDataSetChanged()
            }
        }
    }

    inner class DownloadsViewHolder(private val binding: ItemTrackBinding, private val isWithAd: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: DownloadItem) {
            binding.artistNameText.text = track.artistName
            binding.nameText.text = track.trackName

            val nameTextColorId: Int = if (track.url == currentPlayingTrackUrl) {
                R.color.primary
            } else {
                R.color.primary_text
            }

            binding.nameText.setTextColor(ContextCompat.getColor(itemView.context, nameTextColorId))

            loadTrackCover(track.coverUrl135)

            binding.artistNameText.alpha = 1f
            binding.nameText.alpha = 1f
            binding.coverImage.alpha = 1f
            itemView.onClick {
                GlobalScope.launch(Dispatchers.IO) {
                    onClick(tracks.indexOf(track))
                    withContext(Dispatchers.Main) {
                        this@DownloadsAdapter.notifyDataSetChanged()
                    }
                }
            }

            itemView.onLongClick {
                onLongClick(track.trackId, track.artistId)
            }

            if (isWithAd) {
                binding.root.children.forEach {
                    if (it is TemplateView) {
                        binding.root.removeView(it)
                    }
                }

                val view = LayoutInflater.from(binding.root.context).inflate(
                    R.layout.view_native_ad,
                    binding.root,
                    false
                )

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

        private fun loadTrackCover(coverUrl: String?) {
            val builder: ImageRequest.Builder.() -> Unit = {
                crossfade(CROSSFADE)
                transformations(RoundedCornersTransformation(CORNER_RADIUS))
                diskCachePolicy(CachePolicy.ENABLED)
            }

            coverUrl?.let {
                binding.coverImage.load(uri = coverUrl, builder = builder)
            } ?: binding.coverImage.load(drawableResId = R.drawable.track_placeholder_small, builder = builder)
        }
    }


    companion object {
        const val CROSSFADE = true
        const val CORNER_RADIUS = 12f
        private const val AD_PERIODIZATION = 30
        private const val VIEW_TYPE_AD = 100
        private const val VIEW_TYPE_TRACK = 101
    }
}