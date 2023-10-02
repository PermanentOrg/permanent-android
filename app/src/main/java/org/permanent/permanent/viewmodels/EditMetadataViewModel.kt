package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class EditMetadataViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val onLegacyContactReady = MutableLiveData<List<LegacyContact>>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)
    private lateinit var records: ArrayList<Record>

    fun setRecords(records: ArrayList<Record>) {
        this.records = records
    }
}
