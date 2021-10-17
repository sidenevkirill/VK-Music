package com.flaco_music.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import com.flaco_music.utils.CoverResolutionHelper
import com.flaco_music.utils.firebase.FirebaseManager

class PreferencesImpl(context: Context, private val firebaseManager: FirebaseManager) : Preferences {

    override val isLoggedIn: Boolean
        get() = vkAccessToken.isNotEmpty()

    override var vkAccessToken: String
        get() = preferences.getString(VK_ACCESS_TOKEN, "") ?: ""
        set(value) = preferences.edit().putString(VK_ACCESS_TOKEN, value).apply()

    override var userId: Int
        get() = preferences.getInt(USER_ID, 0)
        set(value) = preferences.edit().putInt(USER_ID, value).apply()

    override var firstName: String
        get() = preferences.getString(FIRST_NAME, "") ?: ""
        set(value) = preferences.edit().putString(FIRST_NAME, value).apply()

    override var lastName: String
        get() = preferences.getString(LAST_NAME, "") ?: ""
        set(value) {
            preferences.edit().putString(LAST_NAME, value).apply()
            firebaseManager.registerUser()
        }

    override var hasSubscription: Boolean
        get() = preferences.getBoolean(HAS_SUBSCRIPTION, false)
        set(value) {
            preferences.edit().putBoolean(HAS_SUBSCRIPTION, value).apply()
            firebaseManager.updateSubscription(value)
        }

    override var currentPlaylistName: String
        get() = preferences.getString(CURRENT_PLAYLIST_NAME, null) ?: ""
        set(value) = preferences.edit().putString(CURRENT_PLAYLIST_NAME, value).apply()

    override var isAutoCachingEnabled: Boolean
        get() = preferences.getBoolean(IS_AUTO_CACHING_ENABLED, false)
        set(value) = preferences.edit().putBoolean(IS_AUTO_CACHING_ENABLED, value).apply()

    override var showNextReleaseIsComingDialog: Boolean
        get() = preferences.getBoolean(SHOW_NEXT_RELEASE_COMING_DIALOG, false)
        set(value) = preferences.edit().putBoolean(SHOW_NEXT_RELEASE_COMING_DIALOG, value).apply()

    override var hasNextReleaseComingDialogBeenShown: Boolean
        get() = preferences.getBoolean(HAS_NEXT_RELEASE_COMING_DIALOG_BEEN_SHOWN, false)
        set(value) = preferences.edit().putBoolean(HAS_NEXT_RELEASE_COMING_DIALOG_BEEN_SHOWN, value).apply()

    override var showNextReleaseIsAvailableDialog: Boolean
        get() = preferences.getBoolean(SHOW_NEXT_RELEASE_AVAILABLE_DIALOG, false)
        set(value) = preferences.edit().putBoolean(SHOW_NEXT_RELEASE_AVAILABLE_DIALOG, value).apply()

    override var coverResolutionIndex: Int
        get() = preferences.getInt(COVER_RESOLUTION_INDEX, CoverResolutionHelper.coverResolutions.lastIndex - 1)
        set(value) = preferences.edit().putInt(COVER_RESOLUTION_INDEX, value).apply()

    override var isDarkModeEnabled: Boolean
        get() = preferences.getBoolean(
            IS_DARK_MODE_ENABLED,
            false,
        )
        set(value) = preferences.edit().putBoolean(IS_DARK_MODE_ENABLED, value).apply()

    override var appLaunches: Int
        get() = preferences.getInt(APP_LAUNCHES, 0)
        set(value) = preferences.edit().putInt(APP_LAUNCHES, value).apply()

    override fun clear() {
        preferences.edit().clear().apply()
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(
        "sharedPreferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val VK_ACCESS_TOKEN = "vk_access_token"
        private const val USER_ID = "user_id"
        private const val FIRST_NAME = "first_name"
        private const val LAST_NAME = "last_name"
        private const val HAS_SUBSCRIPTION = "has_subscription"
        private const val CURRENT_PLAYLIST_NAME = "playlist_name"
        private const val IS_AUTO_CACHING_ENABLED = "is_auto_caching_enabled"
        private const val SHOW_NEXT_RELEASE_COMING_DIALOG = "show_next_release_coming_dialog"
        private const val HAS_NEXT_RELEASE_COMING_DIALOG_BEEN_SHOWN = "has_next_release_coming_dialog_been_shown"
        private const val SHOW_NEXT_RELEASE_AVAILABLE_DIALOG = "show_next_release_available_dialog"
        private const val HAS_NEXT_RELEASE_AVAILABLE_DIALOG_BEEN_SHOWN = "has_next_release_available_dialog_been_shown"
        private const val COVER_RESOLUTION_INDEX = "cover_resolution_index"
        private const val IS_DARK_MODE_ENABLED = "is_dark_mode_enabled"
        private const val APP_LAUNCHES = "app_launches"
    }
}