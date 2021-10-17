package com.flaco_music.retrofit.models

data class TimelineThumbs(
    val count_per_image: Int,
    val count_per_row: Int,
    val count_total: Int,
    val frame_height: Int,
    val frame_width: Double,
    val links: List<String>
)