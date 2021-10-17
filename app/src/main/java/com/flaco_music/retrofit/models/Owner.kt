package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Owner(
    @SerializedName("can_access_closed")
    val canAccessClosed: Boolean,
    @SerializedName("can_invite_to_chats")
    val canInviteToChats: Boolean,
    @SerializedName("name")
    val name: String?,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("first_name_gen")
    val firstNameGen: String?,
    val id: Int,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("photo_100")
    val photo100: String,
    @SerializedName("photo_200")
    val photo200: String,
    @SerializedName("photo_50")
    val photo50: String
)