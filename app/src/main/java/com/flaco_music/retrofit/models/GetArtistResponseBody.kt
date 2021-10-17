package com.flaco_music.retrofit.models

data class GetArtistResponseBody(
    val artist_videos: List<ArtistVideo>,
    val artists: List<Artist>,
    val audios: List<Audio>,
    val catalog: Catalog,
    val links: List<Link>,
    val navigation_tabs: List<Any>,
    val playlists: List<Playlists>,
    val placeholders: List<ArtistPlaceholder>?
)