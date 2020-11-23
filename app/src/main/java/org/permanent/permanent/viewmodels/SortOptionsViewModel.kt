package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.ui.myFiles.SortType

class SortOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var currentSortOption = MutableLiveData<String>()
    private val onSortRequest = MutableLiveData<SortType>()

    fun onSortOptionClicked(sortType: SortType) {
        onSortRequest.value = sortType
    }

    fun getCurrentSortOption(): MutableLiveData<String> {
        return currentSortOption
    }

    fun getOnSortRequest(): MutableLiveData<SortType> {
        return onSortRequest
    }

    fun setCurrentSortOption(backendString: String?) {
        currentSortOption.value = backendString
    }
}