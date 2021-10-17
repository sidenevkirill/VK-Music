package com.flaco_music.ui.screens

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.broadcast.NotificationActionService
import com.flaco_music.broadcast.NotificationActionService.Companion.KEY_PROGRESS
import com.flaco_music.broadcast.NotificationActionService.Companion.KEY_TRACK_POSITION
import com.flaco_music.db.current_playlist.CurrentPlaylistItem
import com.flaco_music.db.current_playlist.CurrentPlaylistRepository
import com.flaco_music.db.my_music_ids.MyMusicIdItem
import com.flaco_music.db.my_music_ids.MyMusicIdsRepository
import com.flaco_music.db.player_config.PlayerConfigRepository
import com.flaco_music.service.PlayerService
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import com.flaco_music.utils.player.PlayerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class MainViewModel(application: Application) : PlayerViewModel(application), KoinComponent {

    val viewPagerPositionLiveData: MutableLiveData<Int> = MutableLiveData()
    val musicProgressLiveData: LiveData<Int> = MutableLiveData()
    val isPlayingLiveData: LiveData<Boolean> = MutableLiveData(true)
    var isPlayerActivatedLiveData: LiveData<Boolean> = MutableLiveData(false)

    val currentPlayingTrack: CurrentPlaylistItem?
        get() = currentPlaylistRepository.currentPlayingTrack

    var showNextReleaseComingDialog: LiveData<Boolean> =
        MutableLiveData(preferences.showNextReleaseIsComingDialog && !preferences.hasNextReleaseComingDialogBeenShown)

    var showNextReleaseAvailableDialog: LiveData<Boolean> =
        MutableLiveData(preferences.showNextReleaseIsAvailableDialog)

    var showRateAppDialog: LiveData<Boolean> =
        MutableLiveData(preferences.appLaunches > 0 && preferences.appLaunches % 5 == 0)

    override var playlistTypeName: String = ""

    private val currentPlaylistRepository: CurrentPlaylistRepository by inject()
    private val myMusicIdsRepository: MyMusicIdsRepository by inject()
    private val playerConfigRepository: PlayerConfigRepository by inject()

    private val isTrackInUserAudios: Boolean
        get() = runBlocking(Dispatchers.IO) {
            currentPlayingTrack?.url in myMusicIdsRepository.getMyMusicIds().map { it.url }
        }

    fun onPlayOrPauseBroadcastReceived() {
        val isPlaying = isPlayingLiveData.value ?: false
        (isPlayingLiveData as MutableLiveData).postValue(!isPlaying)
    }

    fun onPreviousBroadcastReceived() {
        (isPlayingLiveData as MutableLiveData).postValue(true)
    }

    fun onNextBroadcastReceived() {
        (isPlayingLiveData as MutableLiveData).postValue(true)
    }

    fun onSetupTrackDataBroadcastReceived(intent: Intent?) {
        val trackPosition = intent?.extras?.getInt(KEY_TRACK_POSITION) ?: 0
        viewPagerPositionLiveData.postValue(trackPosition)

        (isPlayingLiveData as MutableLiveData).postValue(true)

        isPlayerActivatedLiveData.value?.let {
            if (!it) (isPlayerActivatedLiveData as MutableLiveData).postValue(true)
        }
    }

    fun onProgressChangedBroadcastReceived(intent: Intent) {
        val progress = intent.getIntExtra(KEY_PROGRESS, 0)
        (musicProgressLiveData as MutableLiveData).postValue(progress)
    }

    fun changeTrackProgress(progress: Int, isInTouch: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            if (isInTouch) {
                val action = PlayerService.ACTION_CHANGE_PROGRESS_START
                val intent = Intent(context, NotificationActionService::class.java).setAction(action)
                context.sendBroadcast(intent)
            } else {
                val action = PlayerService.ACTION_CHANGE_PROGRESS_END
                val intent = Intent(context, NotificationActionService::class.java).setAction(action)
                intent.putExtra(KEY_PROGRESS, progress)
                context.sendBroadcast(intent)
            }
        }
    }

    internal fun sendBroadcastPlay() {
        if (isPlayingLiveData.value == true) {
            PlayerUtils.sendBroadcastPause(context)
        } else {
            PlayerUtils.sendBroadcastPlay(context)
        }
    }

    internal fun sendBroadcastPrevious() {
        PlayerUtils.sendBroadcastPrevious(context)
    }

    internal fun sendBroadcastNext() {
        PlayerUtils.sendBroadcastNext(context)
    }

    fun setNextReleaseIsComingDialogIsShown() {
        preferences.hasNextReleaseComingDialogBeenShown = true
    }

    fun observeCurrentPlaylist(callback: (List<CurrentPlaylistItem>) -> Unit) {
        currentPlaylistRepository.currentPlaylistLiveData.observeForever {
            callback(it)
        }
    }

    fun observeIsTrackInUserAudios(callback: (isExists: Boolean) -> Unit) {
        myMusicIdsRepository.isTrackInUserAudios(currentPlayingTrack?.url ?: "").observeForever(callback)
    }

    fun observeRepeatMode(callback: (isExists: Int) -> Unit) {
        playerConfigRepository.getRepeatModeLiveData().observeForever {
            callback(it)
        }
    }

    fun observeShuffleMode(callback: (isExists: Boolean) -> Unit) {
        playerConfigRepository.getShuffleModeEnabledLiveData().observeForever {
            callback(it)
        }
    }

    fun addOrRemoveTrack(onFailure: (message: String) -> Unit) {
        Log.d("qqqqqqqqq", "m: ${currentPlayingTrack?.url}")
        Log.d("qqqqqqqqq", "e: ${runBlocking(Dispatchers.IO) { myMusicIdsRepository.getMyMusicIds() }.map { it.url }}")
        if (isTrackInUserAudios) {
            removeTrackFromAudios(onFailure = { message -> onFailure(message) })
        } else {
            addTrackToAudios(onFailure = { message -> onFailure(message) })
        }
    }

    private fun addTrackToAudios(onFailure: (message: String) -> Unit) {
        val ownerId = currentPlayingTrack?.ownerId ?: -1
        val trackId = currentPlayingTrack?.id ?: -1
        val trackUrl = currentPlayingTrack?.url ?: ""

        val call = audioRepository.addTrack(ownerId, trackId)
        call.invoke(
            onResponseSuccessful = {
                coroutineIO { myMusicIdsRepository.insert(MyMusicIdItem(it.body()?.response ?: -1, trackUrl)) }
            },
            onResponseNotSuccessful = {
                onFailure("Couldn't add track.")
            },
            onFailure = {
                onFailure("Couldn't add track.")
            }
        )
    }

    private fun removeTrackFromAudios(onFailure: (message: String) -> Unit) {
        val ownerId = preferences.userId
        val trackId =
            runBlocking(Dispatchers.IO) { myMusicIdsRepository.getMyMusicIdByUrl(currentPlayingTrack?.url ?: "") }

        val call = audioRepository.deleteTrack(ownerId, trackId)
        call.invoke(
            onResponseSuccessful = {
                coroutineIO { myMusicIdsRepository.delete(trackId) }
            },
            onResponseNotSuccessful = {
                onFailure("Couldn't remove track.")
            },
            onFailure = {
                onFailure("Couldn't remove track.")
            }
        )
    }

    fun toggleRepeatMode() {
        playerConfigRepository.toggleRepeatMode()
    }

    fun toggleShuffleMode() {
        playerConfigRepository.toggleShuffleMode()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}