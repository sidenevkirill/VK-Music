package com.flaco_music.ui.screens.catalog.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flaco_music.retrofit.models.CatalogSectionItem
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class CatalogItemsViewModel : ViewModel(), KoinComponent {

    val titleListData: LiveData<String> = MutableLiveData()
    val itemsLiveData: LiveData<List<CatalogSectionItem>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    private val audioRepository: AudioRepository by inject { parametersOf(true) }

    fun loadItems(sectionId: String) {
        val call = audioRepository.getCatalog()
        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.response?.items?.let { sections ->
                        val section = sections.find { it.id == sectionId }
                        val items = section?.items ?: emptyList()

                        val title = section?.title

                        (titleListData as MutableLiveData).postValue(title)
                        (itemsLiveData as MutableLiveData).postValue(items)
                    }
                }
            },
            onResponseNotSuccessful = { errorResponse ->
                val errorMessage = errorResponse.errorDescription
                (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
            },
            anyway = {
                (isLoadingLiveData as MutableLiveData).postValue(false)
            }
        )
    }

    companion object {
        private const val TAG = "CatalogTracksViewModel"
    }
}