package com.flaco_music.ui.adapters.tracks

import android.content.Context
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
import com.flaco_music.db.downloads.DownloadsRepository
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.utils.AlertDialogManager
import com.flaco_music.utils.extentions.gone
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.extentions.onLongClick
import com.flaco_music.utils.extentions.visible
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TracksAdapter(
    private val tracks: List<Audio>,
    private val displayDuration: Boolean = true,
    private val displaySavedIcon: Boolean = true,
    private val lifecycleOwner: LifecycleOwner,
    private val onClick: (position: Int) -> Unit,
    private val onLongClick: (trackId: Int, ownerId: Int) -> Unit
) : RecyclerView.Adapter<TracksAdapter.TrackViewHolder>(), KoinComponent {

    private var currentPlayingTrackUrl: String? = null

    private val currentPlaylistRepository: CurrentPlaylistRepository by inject()
    private val downloadsRepository: DownloadsRepository by inject()

    private var downloads: List<DownloadItem>? = null

    private var adCounter = 0

    private val adsIds = listOf(
        "ca-app-pub-2056309928986745/1230034348",
        "ca-app-pub-2056309928986745/7603871003",
        "ca-app-pub-2056309928986745/8953693263",
        "ca-app-pub-2056309928986745/9948044888",
        "ca-app-pub-2056309928986745/7412299317"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        observeCurrentPlayingTrack()
        observeDownloads()
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        val binding = ItemTrackBinding.bind(view)
        return TrackViewHolder(binding, viewType == VIEW_TYPE_AD)
    }

    override fun getItemCount(): Int = tracks.size

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
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

    override fun onViewDetachedFromWindow(holder: TrackViewHolder) {
        holder.itemView.clearAnimation()
    }

    private fun observeCurrentPlayingTrack() {
        currentPlaylistRepository.currentPlayingTrackLiveData.observe(lifecycleOwner) {
            if (it?.url != currentPlayingTrackUrl) {
                currentPlayingTrackUrl = it?.url
                this@TracksAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun observeDownloads() {
        downloadsRepository.getDownloadsLiveData().observe(lifecycleOwner) {
            if (it != downloads) {
                downloads = it
                this@TracksAdapter.notifyDataSetChanged()
            }
        }
    }

    inner class TrackViewHolder(private val binding: ItemTrackBinding, private val isWithAd: Boolean) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Audio) {
            binding.artistNameText.text = track.artist
            binding.nameText.text = track.title

            val nameTextColorId: Int = if (track.decryptedUrl == currentPlayingTrackUrl) {
                R.color.primary
            } else {
                R.color.primary_text
            }

            binding.nameText.setTextColor(ContextCompat.getColor(itemView.context, nameTextColorId))

            loadTrackCover(track.album?.thumb?.photo135)

            if (displaySavedIcon && downloadsRepository.checkDoesTrackExist(track.decryptedUrl)) {
                binding.isDownloadedIndicatorImage.visible()
            } else {
                binding.isDownloadedIndicatorImage.gone()
            }

            if (displayDuration) {
                val minutes = track.duration / 60
                val seconds = track.duration - (60 * minutes)

                binding.durationText.visible()
                binding.durationText.text = "$minutes:${if (seconds in 0..9) "0$seconds" else seconds}"
            } else {
                binding.durationText.gone()
            }

            if (track.contentRestricted == 1) {
                binding.artistNameText.alpha = 0.5f
                binding.nameText.alpha = 0.5f
                binding.coverImage.alpha = 0.5f
                itemView.onClick {
                    showContentIsRestrictedDialog(it.context)
                }
                itemView.onLongClick {
                    showContentIsRestrictedDialog(it.context)
                }
            } else {
                binding.artistNameText.alpha = 1f
                binding.nameText.alpha = 1f
                binding.coverImage.alpha = 1f
                itemView.onClick {
                    GlobalScope.launch(Dispatchers.IO) {
                        onClick(tracks.indexOf(track))
                        withContext(Dispatchers.Main) {
                            this@TracksAdapter.notifyDataSetChanged()
                        }
                    }
                }
                itemView.onLongClick {
                    onLongClick(track.id, track.ownerId)
                }
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
                        .withAdListener(object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                binding.root.addView(view)
                            }
                        })
                        .build()

                adLoader.loadAd(AdRequest.Builder().build())

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

        private fun showContentIsRestrictedDialog(context: Context) {
            val alertDialogManager = AlertDialogManager(context)
            alertDialogManager.showDialog(
                title = context.getString(R.string.content_restricted_title),
                message = context.getString(R.string.content_restricted_description)
            )
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