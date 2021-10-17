package com.flaco_music.utils

import com.flaco_music.db.current_playlist.CurrentPlaylistItem
import com.flaco_music.utils.preferences.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object CoverResolutionHelper : KoinComponent {

    private val preferences: Preferences by inject()

    var coverResolutionIndex: Int
        get() = preferences.coverResolutionIndex
        set(value) {
            preferences.coverResolutionIndex = value
        }

    val coverResolutions = listOf(
        "Don\'t display",
        "34x34",
        "68x68",
        "135x135",
        "270x270",
        "300x300",
        "600x600",
        "1200x1200"
    )

    fun getCoverResolution(track: CurrentPlaylistItem): String {
        return when (preferences.coverResolutionIndex) {
            0 -> ""
            1 -> track.coverUrl34
            2 -> track.coverUrl68
            3 -> track.coverUrl135
            4 -> track.coverUrl270
            5 -> track.coverUrl300
            6 -> track.coverUrl600
            7 -> track.coverUrl1200
            else -> throw Exception("Unknown cover resolution index")
        } ?: ""
    }
}