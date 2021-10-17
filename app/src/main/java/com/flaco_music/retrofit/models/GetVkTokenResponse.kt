package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName


data class GetVkTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("webview_access_token")
    val webviewAccessToken: String,
    @SerializedName("webview_access_token_expires_in")
    val webviewAccessTokenExpiresIn: String,
    @SerializedName("webview_refresh_token")
    val webviewRefreshToken: String
)