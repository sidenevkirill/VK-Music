package com.flaco_music.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinApiExtension

class NotificationActionService : BroadcastReceiver() {
    @KoinApiExtension
    override fun onReceive(context: Context, intent: Intent) {
        val newIntent = Intent(INTENT_FILTER).apply {
            putExtra(KEY_ACTION_NAME, intent.action)

            intent.extras?.keySet()?.forEach { key ->
                when (val value = intent.extras!!.get(key)) {
                    is String -> putExtra(key, value)
                    is Int -> putExtra(key, value)
                }
            }
        }
        context.sendBroadcast(newIntent)
    }

    companion object {
        private const val TAG = "NotificationActionServi"

        const val INTENT_FILTER = "player"
        const val KEY_ACTION_NAME = "action_name"
        const val KEY_PROGRESS = "progress"
        const val KEY_TRACK_POSITION = "track_position"
    }
}