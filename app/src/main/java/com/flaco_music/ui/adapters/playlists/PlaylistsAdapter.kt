package com.flaco_music.ui.adapters.playlists

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import com.flaco_music.R
import com.flaco_music.databinding.ItemPlaylistBinding
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.utils.extentions.onClick
import com.flaco_music.utils.extentions.onLongClick

class PlaylistsAdapter(
    private val playlists: List<Playlist>,
    private val onClick: (playlistId: Int, ownerId: Int) -> Unit,
    private val onLongClick: (playlistId: Int, ownerId: Int) -> Unit
) : RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        val binding = ItemPlaylistBinding.bind(view)
        return PlaylistViewHolder(binding)
    }

    override fun getItemCount(): Int = playlists.size

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val track = playlists[position]
        holder.bind(track)
    }

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.nameText.text = playlist.title
            binding.artistNameText.text = playlist.mainArtists?.let { artists ->
                TextUtils.join(", ", artists.map { it.name })
            } ?: ""

            val imageUrl = playlist.photo?.photo300

            if (imageUrl != null) loadPlaylistCover(imageUrl) else loadPlaylistCoverPlaceholder()

            itemView.onClick { onClick(playlist.properPlaylistId, playlist.properOwnerId) }
            itemView.onLongClick { onLongClick(playlist.properPlaylistId, playlist.properOwnerId) }
        }

        private fun loadPlaylistCover(coverUrl: String) {
            binding.coverImage.load(coverUrl) {
                crossfade(CROSSFADE)
                transformations(RoundedCornersTransformation(CORNER_RADIUS))
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }

        private fun loadPlaylistCoverPlaceholder() {
            binding.coverImage.load(R.drawable.playlist_placeholder_medium) {
                crossfade(CROSSFADE)
                transformations(RoundedCornersTransformation(CORNER_RADIUS))
            }
        }
    }

    companion object {
        const val CROSSFADE = true
        const val CORNER_RADIUS = 12f
    }
}