package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("can_access_closed")
    val canAccessClosed: Boolean,
    @SerializedName("can_invite_to_chats")
    val canInviteToChats: Boolean,
    @SerializedName("first_name")
    val firstName: String,
    val id: Int,
    @SerializedName("is_closed")
    val isClosed: Boolean,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("photo_100")
    val photo100: String,
    @SerializedName("photo_200")
    val photo200: String,
    @SerializedName("online")
    val online: Int,
    @SerializedName("online_info")
    val onlineInfo: OnlineInfo,
    @SerializedName("screen_name")
    val screenName: String,
    val sex: Int
)