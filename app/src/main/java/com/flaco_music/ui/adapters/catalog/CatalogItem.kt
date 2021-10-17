package com.flaco_music.ui.adapters.catalog

import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.CatalogSectionItem
import com.flaco_music.retrofit.models.CatalogSectionPlaylist

sealed class CatalogItem(val id: String, val title: String, val type: CatalogItemType)

class CatalogAudiosItem(
    id: String,
    title: String,
    val audios: List<Audio>,
    type: CatalogItemType = CatalogItemType.AUDIOS
) : CatalogItem(id, title, type)

class CatalogPlaylistsItem(
    id: String,
    title: String,
    val playlists: List<CatalogSectionPlaylist>,
    type: CatalogItemType = CatalogItemType.PLAYLISTS
) : CatalogItem(id, title, type)

class CatalogOthersItem(
    id: String,
    title: String,
    val items: List<CatalogSectionItem>,
    type: CatalogItemType = CatalogItemType.OTHERS
) : CatalogItem(id, title, type)

enum class CatalogItemType {
    AUDIOS, PLAYLISTS, OTHERS
}