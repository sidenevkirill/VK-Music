package com.flaco_music.ui.dialogs.add_track_to_playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.flaco_music.R
import com.flaco_music.databinding.ItemOptionsPlaylistBinding
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.utils.extentions.onClick

class OptionsPlaylistsAdapter(
    private val items: List<Playlist>,
    private val callback: (playlistId: Int) -> Unit
) : RecyclerView.Adapter<OptionsPlaylistsAdapter.OptionsPlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_options_playlist, parent, false)
        val binding = ItemOptionsPlaylistBinding.bind(view)
        return OptionsPlaylistViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: OptionsPlaylistViewHolder, position: Int) {
        val track = items[position]
        holder.bind(track)
    }

    inner class OptionsPlaylistViewHolder(private val binding: ItemOptionsPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            val title = playlist.title
            val coverUrl = playlist.photo?.photo68

            binding.title.text = title

            binding.root.onClick {
                callback(playlist.properPlaylistId)
            }

            loadCover(coverUrl)
        }

        private fun loadCover(coverUrl: String?) {
            val builder: ImageRequest.Builder.() -> Unit = {
                crossfade(CROSSFADE)
                transformations(CircleCropTransformation())
                diskCachePolicy(CachePolicy.ENABLED)
            }

            coverUrl?.let {
                binding.coverImage.load(uri = coverUrl, builder = builder)
            } ?: binding.coverImage.load(drawableResId = R.drawable.track_placeholder_small, builder = builder)
        }
    }

    companion object {
        const val CROSSFADE = true
    }
}