package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R

class SelectionOptionsViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val title = MutableLiveData<String>()

    fun setSelectionSize(size: Int?) {
        title.value = appContext.getString(R.string.items_selected, size)
    }

    fun getTitle(): MutableLiveData<String> = title
}