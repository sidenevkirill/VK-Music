package com.flaco_music.utils.constants

import android.content.Context
import com.flaco_music.R

object ApiConstants {
    const val API_VERSION_CODE = "5.138"

    object Token {
        const val CLIENT_ID = "2274003"
        const val CLIENT_SECRET = "hHbZxrka2uZ6jB1inYsH"
    }

    object Genres {
        fun get(context: Context) = listOf(
            Genre(1, context.getString(R.string.genre_rock)),
            Genre(2, context.getString(R.string.genre_pop)),
            Genre(3, context.getString(R.string.genre_rap_and_hip_hop)),
            Genre(4, context.getString(R.string.genre_easy_listening)),
            Genre(5, context.getString(R.string.genre_dance_and_house)),
            Genre(6, context.getString(R.string.genre_instrumental)),
            Genre(7, context.getString(R.string.genre_metal)),
            Genre(8, context.getString(R.string.genre_dubstep)),
            Genre(9, context.getString(R.string.genre_jazz_and_blues)),
            Genre(10, context.getString(R.string.genre_drum_and_bass)),
            Genre(11, context.getString(R.string.genre_trance)),
            Genre(12, context.getString(R.string.genre_chanson)),
            Genre(13, context.getString(R.string.genre_ethnic)),
            Genre(14, context.getString(R.string.genre_acoustic_and_vocal)),
            Genre(15, context.getString(R.string.genre_reggae)),
            Genre(16, context.getString(R.string.genre_classical)),
            Genre(17, context.getString(R.string.genre_indie_pop)),
            Genre(18, context.getString(R.string.genre_other)),
            Genre(19, context.getString(R.string.genre_speech)),
            Genre(21, context.getString(R.string.genre_alternative)),
            Genre(22, context.getString(R.string.genre_electropop_and_disco))
        )

        data class Genre(
            val id: Int,
            val name: String
        )
    }
}