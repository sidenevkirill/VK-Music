package com.flaco_music.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadata
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.flaco_music.R
import com.flaco_music.broadcast.NotificationActionService
import com.flaco_music.broadcast.NotificationActionService.Companion.INTENT_FILTER
import com.flaco_music.broadcast.NotificationActionService.Companion.KEY_ACTION_NAME
import com.flaco_music.broadcast.NotificationActionService.Companion.KEY_PROGRESS
import com.flaco_music.broadcast.NotificationActionService.Companion.KEY_TRACK_POSITION
import com.flaco_music.db.current_playlist.CurrentPlaylistItem
import com.flaco_music.db.current_playlist.CurrentPlaylistRepository
import com.flaco_music.db.downloads.DownloadsRepository
import com.flaco_music.db.player_config.PlayerConfigRepository
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.downloader.Downloader
import com.flaco_music.utils.extentions.coroutineDefault
import com.flaco_music.utils.extentions.coroutineMain
import com.flaco_music.utils.player.PlayerUtils
import com.flaco_music.utils.preferences.Preferences
import com.google.android.exoplayer2.*
import kotlinx.coroutines.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@KoinApiExtension
class PlayerService : LifecycleService(), KoinComponent, Player.Listener {

    private var updateProgressJob: Job? = null
    private var handleOnStartCommandJob: Job? = null
    private var isCurrentTrackPositionChanged: Boolean = false

    private var currentPlaylistName: String = ""
        set(value) {
            field = value
            preferences.currentPlaylistName = value
        }

    private var playbackSource: PlaybackSource = PlaybackSource.WEB
    private var currentTrackPosition: Int = -1
        get() = try {
            currentPlaylistRepository.currentPlayingTrack.position
        } catch (e: java.lang.Exception) {
            -1
        }
        set(value) {
            if (field != value || isCurrentPlaylistChanged) {
                field = value
                Log.d(TAG, "${value}: ")
                isCurrentTrackPositionChanged = true
                currentPlaylistRepository.setCurrentPlayingTrack(value)
            }
        }


    private lateinit var exoPlayer: ExoPlayer
    private lateinit var notificationBuilder: MusicNotificationBuilder

    private val preferences: Preferences by inject()
    private val downloader: Downloader by inject()
    private val snackbarManager: SnackbarManager by inject()

    private val downloadsRepository: DownloadsRepository by inject()
    private val currentPlaylistRepository: CurrentPlaylistRepository by inject()
    private val playerConfigRepository: PlayerConfigRepository by inject()

    private val isCurrentPlaylistChanged: Boolean
        get() = preferences.currentPlaylistName != currentPlaylistName

    private val currentTrack: CurrentPlaylistItem?
        get() = currentPlaylistRepository.currentPlayingTrack

    private var currentPlaylistSize: Int = 0

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, intent: Intent?) {
            when (intent?.extras?.getString(KEY_ACTION_NAME)) {
                ACTION_PLAY -> play()
                ACTION_PAUSE -> pause()
                ACTION_PREVIOUS -> previous()
                ACTION_NEXT -> next()
                ACTION_CHANGE_PROGRESS_START -> pause()
                ACTION_CHANGE_PROGRESS_END -> {
                    val progress = (exoPlayer.duration / 1000) * intent.getIntExtra(KEY_PROGRESS, 0)
                    exoPlayer.seekTo(progress)
                    exoPlayer.play()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver()
        createChannel()

        playerConfigRepository.getRepeatModeLiveData().observe(this) { repeatMode ->
            if (this::exoPlayer.isInitialized) {
                exoPlayer.repeatMode = repeatMode
            }
        }

        playerConfigRepository.getShuffleModeEnabledLiveData().observe(this) { shuffleMode ->
            if (this::exoPlayer.isInitialized) {
                exoPlayer.shuffleModeEnabled = shuffleMode
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleOnStartCommand(intent)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
        unregisterReceiver()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        coroutineDefault {
            try {
                createNotification(currentTrack ?: return@coroutineDefault)
            } catch (e: Exception) {
                // do nothing
            }
        }

        if (isPlaying) {
            updateProgressJob = coroutineMain { updateProgress() }
            updateProgressJob?.start()
        } else {
            updateProgressJob?.cancel()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        snackbarManager.showSnackbar(getString(R.string.playback_error), SnackbarManager.SnackbarType.ERROR)
        playbackSource = PlaybackSource.WEB
        setupPlayer(playbackSource)
    }

    private fun handleOnStartCommand(intent: Intent?) {
        handleOnStartCommandJob?.cancel()
        handleOnStartCommandJob = coroutineMain {
            val extras = intent?.extras
            currentTrackPosition = extras?.getInt(KEY_POSITION) ?: -1
            if (this@PlayerService::exoPlayer.isInitialized) {
                if (isCurrentPlaylistChanged) {
                    val newPlaybackSource = if (extras?.getBoolean(KEY_IS_FROM_STORAGE) == true) {
                        PlaybackSource.STORAGE
                    } else {
                        PlaybackSource.WEB
                    }
                    setupPlayer(newPlaybackSource)
                } else {
                    if (isCurrentTrackPositionChanged) {
                        if (currentTrackPosition != -1) {
                            exoPlayer.seekTo(currentTrackPosition, C.TIME_UNSET)
                            play()
                            isCurrentTrackPositionChanged = false
                        }
                    } else {
                        if (exoPlayer.isPlaying) {
                            PlayerUtils.sendBroadcastPause(applicationContext)
                        } else {
                            PlayerUtils.sendBroadcastPlay(applicationContext)
                        }
                    }
                }
            } else {
                val newPlaybackSource = if (extras?.getBoolean(KEY_IS_FROM_STORAGE) == true) {
                    PlaybackSource.STORAGE
                } else {
                    PlaybackSource.WEB
                }
                setupPlayer(newPlaybackSource)
            }

            if (preferences.isAutoCachingEnabled) {
                downloader.saveCurrentPlaylistItem(currentTrack ?: return@coroutineMain)
            }
        }
        handleOnStartCommandJob?.start()
    }

    private fun setupPlayer(newPlaybackSource: PlaybackSource) {
        currentPlaylistName = preferences.currentPlaylistName
        stopPlayerIfNeeded()
        exoPlayer = SimpleExoPlayer.Builder(this).build()
        if (!this::notificationBuilder.isInitialized) {
            notificationBuilder = MusicNotificationBuilder()
        }

        if (playbackSource != newPlaybackSource) {
            playbackSource = newPlaybackSource
        }

        when (playbackSource) {
            PlaybackSource.WEB -> exoPlayer.setupWithWeb()
            PlaybackSource.STORAGE -> exoPlayer.setupWithStorage()
        }

        exoPlayer.repeatMode = playerConfigRepository.getRepeatMode()
        exoPlayer.shuffleModeEnabled = playerConfigRepository.getShuffleModeEnabled()
        exoPlayer.addListener(this)

        try {
            exoPlayer.seekTo(currentTrackPosition, C.TIME_UNSET)
            exoPlayer.prepare()
            play()
        } catch (e: IllegalSeekPositionException) {
            GlobalScope.launch(Dispatchers.Main) {
                delay(1000)
                setupPlayer(playbackSource)
            }
        }
    }

    private suspend fun createNotification(track: CurrentPlaylistItem) = coroutineScope {
        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)

        notificationBuilder.name = track.name
        notificationBuilder.artist = track.artist
        notificationBuilder.coverUrl = track.coverUrl135

        val notification = notificationBuilder.build()

        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val intent = PendingIntent.getActivity(
            applicationContext, 0,
            notificationIntent, 0
        )

        notification.contentIntent = intent

        withContext(Dispatchers.Main) {
            notificationManagerCompat.notify(1, notification)
            startForeground(1, notification)

            if (!exoPlayer.isPlaying) {
                stopForeground(false)
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun play() {
        exoPlayer.play()

        isCurrentTrackPositionChanged = false

        val intentSendTrackPosition = Intent(applicationContext, NotificationActionService::class.java).apply {
            action = ACTION_SETUP_TRACK_DATA
            putExtra(KEY_TRACK_POSITION, currentTrackPosition)
        }

        sendBroadcast(intentSendTrackPosition)
    }

    private fun pause() {
        exoPlayer.pause()
    }

    private fun previous() {
        if (exoPlayer.hasPrevious()) {
            if (currentTrack?.isContentRestricted != false) {
                previous()
            }

            exoPlayer.previous()
            currentTrackPosition = exoPlayer.currentWindowIndex
            play()
        }
    }

    private fun next() {
        if (exoPlayer.hasNext()) {
            if (currentTrack?.isContentRestricted != false) next()

            exoPlayer.next()
            currentTrackPosition = exoPlayer.currentWindowIndex
            play()
        }
    }

    private fun ExoPlayer.setupWithWeb() {
        val tracks = currentPlaylistRepository.currentPlaylist
        currentPlaylistSize = tracks.size
        tracks.map { it.url }.forEach { url ->
            try {
                val mediaItem = MediaItem.fromUri(url)
                addMediaItem(mediaItem)
            } catch (e: OutOfMemoryError) {
            }
        }
    }

    private fun ExoPlayer.setupWithStorage() {
        val downloads = downloadsRepository.getDownloads()
        currentPlaylistSize = downloads.size
        downloads.map { it.trackId }.forEach { id ->
            val mediaItem = MediaItem.fromUri("${downloader.DIRECTORY_PATH}/$id.mp3".toUri())
            Log.d(TAG, "setupWithStorage: ${mediaItem.playbackProperties?.uri.toString()}")
            addMediaItem(mediaItem)
        }
    }

    private suspend fun updateProgress(): Unit = coroutineScope {
        val duration = exoPlayer.duration.toInt()
        val currentPosition = exoPlayer.currentPosition.toInt()

        playNextIfNeeded(duration, currentPosition)

        val progress = if (currentPosition != 0) {
            (currentPosition * 1000) / duration
        } else 0

        val action = ACTION_PROGRESS_CHANGED
        val intent =
            Intent(applicationContext, NotificationActionService::class.java).setAction(action)
        intent.putExtra(KEY_PROGRESS, progress)
        sendBroadcast(intent)

        delay(1000)

        updateProgress()
    }

    private fun stopPlayerIfNeeded() {
        if (this::exoPlayer.isInitialized) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    private suspend fun playNextIfNeeded(duration: Int, currentPosition: Int) = coroutineScope {
        if (duration > 1
            && currentPosition in (duration - 1000)..duration
            && exoPlayer.repeatMode != Player.REPEAT_MODE_ONE
        ) {
            withContext(Dispatchers.Main) {
                next()
            }
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(INTENT_FILTER)
        registerReceiver(receiver, intentFilter)
    }

    private fun unregisterReceiver() {
        unregisterReceiver(receiver)
    }

    inner class MusicNotificationBuilder {

        var name: String = ""
            set(value) {
                field = value
                mediaSessionCompat.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, name)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                        .build()
                )
                notificationStyle.setMediaSession(mediaSessionCompat.sessionToken)
                builder.setContentTitle(value)
                builder.setStyle(notificationStyle)
            }

        var artist: String = ""
            set(value) {
                field = value
                mediaSessionCompat.setMetadata(
                    MediaMetadataCompat.Builder()
                        .putString(MediaMetadata.METADATA_KEY_TITLE, name)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                        .build()
                )
                notificationStyle.setMediaSession(mediaSessionCompat.sessionToken)
                builder.setContentText(value)
                builder.setStyle(notificationStyle)
            }

        var coverUrl: String? = ""
            set(value) = runBlocking(Dispatchers.IO) {
                field = value

                if (value.isNullOrEmpty()) {
                    val bitmap =
                        ContextCompat.getDrawable(applicationContext, R.drawable.track_placeholder_small)?.toBitmap()
                    builder.setLargeIcon(bitmap)
                    return@runBlocking
                }

                val loader = ImageLoader(applicationContext)
                val req = ImageRequest.Builder(applicationContext)
                    .data(value)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .target { result ->
                        val bitmap = (result as BitmapDrawable).bitmap
                        builder.setLargeIcon(bitmap)
                    }
                    .build()

                loader.execute(req)
            }

        private val builder: Builder by lazy {
            Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note_grey_24)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setOngoing(false)
                .setSilent(true)
                .setStyle(notificationStyle)
                .setPriority(PRIORITY_HIGH)
        }

        private val actionPrevious: Action by lazy {
            val intentPrevious: Intent =
                Intent(applicationContext, NotificationActionService::class.java)
                    .setAction(ACTION_PREVIOUS)
            val pendingIntentPrevious = PendingIntent.getBroadcast(
                applicationContext, 0,
                intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT
            )

            Action(R.drawable.exo_notification_previous, getString(R.string.previous), pendingIntentPrevious)
        }

        private val actionNext: Action by lazy {
            val intentNext: Intent = Intent(applicationContext, NotificationActionService::class.java)
                .setAction(PlayerService.ACTION_NEXT)
            val pendingIntentNext = PendingIntent.getBroadcast(
                applicationContext, 0,
                intentNext, PendingIntent.FLAG_UPDATE_CURRENT
            )

            Action(R.drawable.exo_notification_next, getString(R.string.next), pendingIntentNext)
        }

        private val actionPlay: Action
            get() = runBlocking(Dispatchers.Main) {

                val intentPlay: Intent = Intent(applicationContext, NotificationActionService::class.java)
                    .setAction(ACTION_PLAY)
                val pendingIntentPlay = PendingIntent.getBroadcast(
                    applicationContext, 0,
                    intentPlay, PendingIntent.FLAG_UPDATE_CURRENT
                )

                val intentPause: Intent = Intent(applicationContext, NotificationActionService::class.java)
                    .setAction(PlayerService.ACTION_PAUSE)
                val pendingIntentPause = PendingIntent.getBroadcast(
                    applicationContext, 0,
                    intentPause, PendingIntent.FLAG_UPDATE_CURRENT
                )

                if (exoPlayer.isPlaying) {
                    Action(R.drawable.exo_notification_pause, getString(R.string.pause), pendingIntentPause)
                } else {
                    Action(R.drawable.exo_notification_play, getString(R.string.play), pendingIntentPlay)
                }
            }

        private val mediaSessionCompat = MediaSessionCompat(applicationContext, "media_session").apply {
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())
            isActive = true
        }

        private val notificationStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)

        @SuppressLint("RestrictedApi")
        fun build(): Notification {
            builder.mActions.clear()
            builder.addAction(actionPrevious)
            builder.addAction(actionPlay)
            builder.addAction(actionNext)
            return builder.build()
        }
    }

    private enum class PlaybackSource {
        WEB, STORAGE
    }

    companion object {
        private const val TAG = "PlayerService"

        const val CHANNEL_ID = "app.MUSIC"

        const val ACTION_PLAY = "player.PLAY"
        const val ACTION_PAUSE = "player.PAUSE"
        const val ACTION_PREVIOUS = "player.PREVIOUS"
        const val ACTION_NEXT = "player.NEXT"
        const val ACTION_CHANGE_PROGRESS_START = "player.CHANGE_PROGRESS_START"
        const val ACTION_CHANGE_PROGRESS_END = "player.CHANGE_PROGRESS_END"
        const val ACTION_PROGRESS_CHANGED = "player.PROGRESS_CHANGED"
        const val ACTION_SETUP_TRACK_DATA = "player.SETUP_TRACK_DATA"

        const val KEY_POSITION = "position"
        const val KEY_IS_FROM_STORAGE = "is_from_storage"

        private const val CHANNEL_NAME = "Flaco Player Service"

        private var instance: PlayerService? = null

        fun startMe(context: Context, position: Int, isFromStorage: Boolean) {
            val playerServiceIntent = Intent(context, PlayerService::class.java)
            playerServiceIntent.putExtra(KEY_POSITION, position)
            playerServiceIntent.putExtra(KEY_IS_FROM_STORAGE, isFromStorage)
            getInstance().stopSelf()
            context.startService(playerServiceIntent)
        }

        private fun getInstance(): PlayerService {
            return if (instance != null) {
                instance
            } else {
                instance = PlayerService()
                instance
            }!!
        }
    }
}
