package com.flaco_music.koin

import androidx.room.Room
import com.flaco_music.db.FlacoMusicDatabase
import com.flaco_music.db.current_playlist.CurrentPlaylistRepository
import com.flaco_music.db.downloads.DownloadsRepository
import com.flaco_music.db.my_music_ids.MyMusicIdsRepository
import com.flaco_music.db.player_config.PlayerConfigRepository
import com.flaco_music.db.search_history.SearchHistoryRepository
import com.flaco_music.retrofit.FlacoMusicService
import com.flaco_music.retrofit.VkService
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.retrofit.repository.VkTokenRepository
import com.flaco_music.ui.dialogs.artist_options.ArtistOptionsViewModel
import com.flaco_music.ui.dialogs.playlist_options.PlaylistOptionsViewModel
import com.flaco_music.ui.screens.MainViewModel
import com.flaco_music.ui.screens.artist.ArtistViewModel
import com.flaco_music.ui.screens.catalog.items.CatalogItemsViewModel
import com.flaco_music.ui.screens.catalog.playlists.CatalogPlaylistsViewModel
import com.flaco_music.ui.screens.catalog.tracks.CatalogTracksViewModel
import com.flaco_music.ui.screens.downloads.DownloadsViewModel
import com.flaco_music.ui.screens.explore.ExploreViewModel
import com.flaco_music.ui.screens.feedback.FeedbackViewModel
import com.flaco_music.ui.screens.friend_music.FriendMusicViewModel
import com.flaco_music.ui.screens.genre.GenreViewModel
import com.flaco_music.ui.screens.home.HomeViewModel
import com.flaco_music.ui.screens.login.LoginViewModel
import com.flaco_music.ui.screens.my_music.MyMusicViewModel
import com.flaco_music.ui.screens.options.OptionsViewModel
import com.flaco_music.ui.screens.playlist.PlaylistViewModel
import com.flaco_music.ui.screens.search.SearchViewModel
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.downloader.Downloader
import com.flaco_music.utils.firebase.FirebaseManager
import com.flaco_music.utils.preferences.Preferences
import com.flaco_music.utils.preferences.PreferencesImpl
import com.flaco_music.utils.resource.ResourceManager
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module

@OptIn(KoinApiExtension::class)
val module = module {
    // API
    single { VkTokenRepository(VkService().vkTokenApi) }
    single { (isCachingEnabled: Boolean) ->
        AudioRepository(FlacoMusicService(androidContext(), isCachingEnabled).audioApi)
    }

    // SharedPreferences
    single<Preferences> { PreferencesImpl(androidContext(), get()) }

    // ViewModel
    single { MainViewModel(androidApplication()) }
    single { LoginViewModel(androidApplication()) }
    single { MyMusicViewModel(androidApplication()) }
    single { PlaylistViewModel(androidApplication()) }
    single { ExploreViewModel(androidApplication()) }
    single { GenreViewModel(androidApplication()) }
    single { HomeViewModel(androidApplication()) }
    single { SearchViewModel(androidApplication()) }
    single { CatalogTracksViewModel(androidApplication()) }
    single { CatalogPlaylistsViewModel() }
    single { CatalogItemsViewModel() }
    single { FriendMusicViewModel(androidApplication()) }
    single { ArtistViewModel(androidApplication()) }
    single { OptionsViewModel() }
    single { DownloadsViewModel(androidApplication()) }
    single { ArtistOptionsViewModel(androidApplication()) }
    single { PlaylistOptionsViewModel(androidApplication()) }
    single { FeedbackViewModel() }

    // Room
    single {
        return@single Room.databaseBuilder(
            androidContext(), FlacoMusicDatabase::class.java, "android_questions_database"
        ).fallbackToDestructiveMigration().build()
    }

    single {
        val db = get() as FlacoMusicDatabase
        return@single CurrentPlaylistRepository(db.technologiesDao)
    }

    single {
        val db = get() as FlacoMusicDatabase
        return@single DownloadsRepository(db.downloadsDao)
    }

    single {
        val db = get() as FlacoMusicDatabase
        return@single MyMusicIdsRepository(db.myMusicIdsDao)
    }

    single {
        val db = get() as FlacoMusicDatabase
        return@single PlayerConfigRepository(db.playerConfigDao)
    }

    single {
        val db = get() as FlacoMusicDatabase
        return@single SearchHistoryRepository(db.searchHistoryDao)
    }

    single { Downloader(androidContext()) }

    // SnackbarManager
    single { SnackbarManager() }

    // ResourceManager
    single { ResourceManager(androidContext()) }

    // FirebaseManager
    single { FirebaseManager(androidContext()) }

    // RateDialogManager
    single { com.flaco_music.utils.RateDialogManager }
}