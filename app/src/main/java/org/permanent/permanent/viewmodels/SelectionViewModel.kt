package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Record

abstract class SelectionViewModel(application: Application) : RelocationViewModel(application) {

    private val appContext = application.applicationContext
    val isSelectionMode = MutableLiveData(false)
    val showActionIsland = MutableLiveData(false)
    val areAllSelected = MutableLiveData(false)
    val selectBtnText = MutableLiveData(application.getString(R.string.button_select))
    val selectedRecords = MutableLiveData<MutableList<Record>>(ArrayList())
    val selectedRecordsSize = MutableLiveData(0)
    private val expandIslandRequest = SingleLiveEvent<Void>()

    fun onSelectBtnClick() {
        isSelectionMode.value = true
        selectBtnText.value = appContext.getString(R.string.button_select_all)
    }

    fun getExpandIslandRequest(): SingleLiveEvent<Void> = expandIslandRequest

    companion object {
        const val DELAY_TO_POPULATE_ISLAND_MILLIS = 400L
    }
}