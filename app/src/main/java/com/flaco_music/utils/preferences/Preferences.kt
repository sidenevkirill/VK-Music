package com.flaco_music.utils.preferences

interface Preferences {

    val isLoggedIn: Boolean

    var vkAccessToken: String

    var userId: Int

    var firstName: String

    var lastName: String

    var hasSubscription: Boolean

    var currentPlaylistName: String

    var isAutoCachingEnabled: Boolean

    var showNextReleaseIsComingDialog: Boolean

    var hasNextReleaseComingDialogBeenShown: Boolean

    var showNextReleaseIsAvailableDialog: Boolean

    var coverResolutionIndex: Int

    var isDarkModeEnabled: Boolean

    var appLaunches: Int

    fun clear()
}