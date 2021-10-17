package com.flaco_music.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flaco_music.db.current_playlist.CurrentPlaylistDao
import com.flaco_music.db.current_playlist.CurrentPlaylistItem
import com.flaco_music.db.downloads.DownloadItem
import com.flaco_music.db.downloads.DownloadsDao
import com.flaco_music.db.my_music_ids.MyMusicIdItem
import com.flaco_music.db.my_music_ids.MyMusicIdsDao
import com.flaco_music.db.player_config.PlayerConfig
import com.flaco_music.db.player_config.PlayerConfigDao
import com.flaco_music.db.search_history.SearchHistoryDao
import com.flaco_music.ui.adapters.search.Album
import com.flaco_music.ui.adapters.search.Artist
import com.flaco_music.ui.adapters.search.Audio

@Database(
    entities = [
        CurrentPlaylistItem::class,
        DownloadItem::class,
        MyMusicIdItem::class,
        PlayerConfig::class,
        Audio::class,
        Album::class,
        Artist::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FlacoMusicDatabase : RoomDatabase() {
    abstract val technologiesDao: CurrentPlaylistDao
    abstract val downloadsDao: DownloadsDao
    abstract val myMusicIdsDao: MyMusicIdsDao
    abstract val playerConfigDao: PlayerConfigDao
    abstract val searchHistoryDao: SearchHistoryDao
}