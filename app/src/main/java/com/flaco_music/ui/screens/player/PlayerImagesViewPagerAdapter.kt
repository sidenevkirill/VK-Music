package com.flaco_music.ui.screens.player

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.flaco_music.R
import com.flaco_music.databinding.ImageSliderLayoutBinding

class PlayerImagesViewPagerAdapter(val items: List<String?>) :
    RecyclerView.Adapter<PlayerImagesViewPagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ImageSliderLayoutBinding.inflate(layoutInflater)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ImageViewHolder(binding: ImageSliderLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        private val imageView: ImageView = binding.sliderImageView

        fun bind(url: String?) {
            if (url.isNullOrEmpty()) {
                loadTrackCoverPlaceHolder()
            } else {
                loadTrackCover(url)
            }
        }

        private fun loadTrackCover(coverUrl: String) {
            imageView.load(coverUrl) {
                crossfade(true)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }

        private fun loadTrackCoverPlaceHolder() {
            imageView.load(R.drawable.track_placeholder_big) {
                crossfade(true)
            }
        }
    }
}