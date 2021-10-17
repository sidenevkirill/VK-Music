package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    val error: String,
    @SerializedName("error_description")
    val errorDescription: String,
    @SerializedName("error_type")
    val errorType: String
)