package com.flaco_music.utils.player

import android.content.Context
import android.content.Intent
import com.flaco_music.broadcast.NotificationActionService
import com.flaco_music.service.PlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object PlayerUtils {
    fun sendBroadcastPlay(context: Context) {
        sendBroadcast(context, PlayerService.ACTION_PLAY)
    }

    fun sendBroadcastPause(context: Context) {
        sendBroadcast(context, PlayerService.ACTION_PAUSE)
    }

    fun sendBroadcastPrevious(context: Context) {
        sendBroadcast(context, PlayerService.ACTION_PREVIOUS)
    }

    fun sendBroadcastNext(context: Context) {
        sendBroadcast(context, PlayerService.ACTION_NEXT)
    }

    private fun sendBroadcast(context: Context, action: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val intent: Intent = Intent(context, NotificationActionService::class.java).setAction(action)
            context.sendBroadcast(intent)
        }
    }
}