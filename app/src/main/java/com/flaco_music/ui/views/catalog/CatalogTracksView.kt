package com.flaco_music.ui.views.catalog

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import com.flaco_music.databinding.ViewCatalogTracksBinding
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.ui.adapters.tracks.TracksAdapter
import com.flaco_music.utils.extentions.onClick

class CatalogTracksView(
    context: Context,
    override var title: String,
    private val viewPosition: Int,
    private val lifecycleOwner: LifecycleOwner,
    private val onTrackClicked: (position: Int) -> Unit,
    private val onTrackLongClicked: (trackId: Int, ownerId: Int) -> Unit,
    override val onShowAllClicked: () -> Unit
) : CatalogView(context) {

    var audios: List<Audio>? = null
        set(value) {
            field = value
            value?.let { setupListAdapter(it) }
        }

    override val binding: ViewCatalogTracksBinding = ViewCatalogTracksBinding.inflate(
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

    private fun setupListAdapter(tracks: List<Audio>) {
        val adapter = TracksAdapter(
            tracks = tracks,
            displayDuration = false,
            displaySavedIcon = false,
            lifecycleOwner = lifecycleOwner,
            onClick = { position ->
                val range = (6 * viewPosition)..(6 * viewPosition + 6)
                val p = range.toList()[position]
                onTrackClicked(p)
            },
            onLongClick = { trackId, ownerId ->
                onTrackLongClicked(trackId, ownerId)
            }
        )
        binding.tracksRecycler.adapter = adapter
    }

    companion object {
        private const val TAG = "CatalogTracksView"
    }
}
