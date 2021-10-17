package com.flaco_music.db.player_config

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PlayerConfigRepository(private val playerConfigDao: PlayerConfigDao) {

    fun getRepeatMode(): Int {
        return runBlocking(Dispatchers.IO) { playerConfigDao.getRepeatMode() }
    }

    fun getShuffleModeEnabled(): Boolean {
        return runBlocking(Dispatchers.IO) { playerConfigDao.getShuffleModeEnabled() }
    }

    fun getRepeatModeLiveData(): LiveData<Int> {
        return playerConfigDao.getRepeatModeLiveData()
    }

    fun getShuffleModeEnabledLiveData(): LiveData<Boolean> {
        return playerConfigDao.getShuffleModeEnabledLiveData()
    }

    fun create() {
        GlobalScope.launch(Dispatchers.IO) {
            playerConfigDao.clear()
            playerConfigDao.insert(PlayerConfig())
        }
    }

    fun toggleRepeatMode() {
        GlobalScope.launch(Dispatchers.IO) { playerConfigDao.updateRepeatMode() }
    }

    fun toggleShuffleMode() {
        GlobalScope.launch(Dispatchers.IO) { playerConfigDao.updateShuffleMode() }
    }

    fun setShuffleMode(isShuffle: Boolean) {
        GlobalScope.launch(Dispatchers.IO) { playerConfigDao.setShuffleMode(isShuffle) }
    }
}