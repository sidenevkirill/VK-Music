package com.flaco_music.utils.options

import android.content.Context
import com.flaco_music.R

class OptionsUtils(private val context: Context) {

    fun createPlaylistOptions(vararg options: Pair<OptionType, () -> Unit>): MutableList<Option> {
        return options.map { option ->
            Option(option.first, getOptionIcon(option.first), getOptionTitle(option.first), option.second)
        }.toMutableList()
    }

    private fun getOptionIcon(optionType: OptionType): Int {
        return when (optionType) {
            OptionType.ADD_TO_MY_MUSIC_ID -> R.drawable.ic_baseline_add_24
            OptionType.REMOVE_FROM_MY_MUSIC_ID -> R.drawable.ic_baseline_remove_circle_outline_24
            OptionType.FOLLOW_ID -> R.drawable.ic_baseline_add_24
            OptionType.UNFOLLOW_ID -> R.drawable.ic_baseline_delete_24
            OptionType.ADD_TO_PLAYLIST_ID -> R.drawable.ic_baseline_playlist_add_24
            OptionType.REMOVE_FROM_PLAYLIST_ID -> R.drawable.ic_baseline_playlist_remove_24
            OptionType.SAVE_ID -> R.drawable.ic_baseline_save_24_black
            OptionType.REMOVE_FROM_SAVED_ID -> R.drawable.ic_baseline_delete_24
            OptionType.LYRICS_ID -> R.drawable.ic_baseline_text_snippet_24
            OptionType.VIEW_ALBUM_ID -> R.drawable.ic_baseline_album_24
            OptionType.VIEW_ARTIST_ID -> R.drawable.ic_baseline_person_24
            OptionType.SHARE_ID -> R.drawable.ic_baseline_share_24
            OptionType.SHUFFLE_PLAY -> R.drawable.exo_styled_controls_shuffle_on
        }
    }

    private fun getOptionTitle(optionType: OptionType): String {
        return when (optionType) {
            OptionType.ADD_TO_MY_MUSIC_ID -> context.getString(R.string.dialog_option_add_to_my_music)
            OptionType.REMOVE_FROM_MY_MUSIC_ID -> context.getString(R.string.dialog_option_remove_from_my_music)
            OptionType.FOLLOW_ID -> context.getString(R.string.dialog_option_follow)
            OptionType.UNFOLLOW_ID -> context.getString(R.string.dialog_option_unfollow)
            OptionType.ADD_TO_PLAYLIST_ID -> context.getString(R.string.dialog_option_add_to_playlist)
            OptionType.REMOVE_FROM_PLAYLIST_ID -> context.getString(R.string.dialog_option_remove_from_playlist)
            OptionType.SAVE_ID -> context.getString(R.string.dialog_option_save)
            OptionType.REMOVE_FROM_SAVED_ID -> context.getString(R.string.dialog_option_remove_from_saved)
            OptionType.LYRICS_ID -> context.getString(R.string.dialog_option_lyrics)
            OptionType.VIEW_ALBUM_ID -> context.getString(R.string.dialog_option_view_album)
            OptionType.VIEW_ARTIST_ID -> context.getString(R.string.dialog_option_view_artist)
            OptionType.SHARE_ID -> context.getString(R.string.dialog_option_share)
            OptionType.SHUFFLE_PLAY -> context.getString(R.string.dialog_option_shuffle_play)
        }
    }

    data class Option(
        val type: OptionType,
        val iconRes: Int,
        val title: String,
        val onClick: () -> Unit
    )

    enum class OptionType {
        ADD_TO_MY_MUSIC_ID,
        REMOVE_FROM_MY_MUSIC_ID,
        FOLLOW_ID,
        UNFOLLOW_ID,
        ADD_TO_PLAYLIST_ID,
        REMOVE_FROM_PLAYLIST_ID,
        VIEW_ALBUM_ID,
        VIEW_ARTIST_ID,
        SAVE_ID,
        REMOVE_FROM_SAVED_ID,
        LYRICS_ID,
        SHARE_ID,
        SHUFFLE_PLAY
    }
}