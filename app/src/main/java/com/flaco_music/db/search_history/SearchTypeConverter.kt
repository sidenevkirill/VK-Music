package com.flaco_music.db.search_history

import androidx.room.TypeConverter
import com.flaco_music.ui.adapters.search.SearchItemType

class SearchTypeConverter {

    @TypeConverter
    fun toSearchType(value: String?): SearchItemType? = SearchItemType.valueOf(value!!)

    @TypeConverter
    fun fromSearchType(searchType: SearchItemType?): String? = searchType.toString()
}
