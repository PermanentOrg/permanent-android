package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.ILegacyArchiveListener
import org.permanent.permanent.network.ILegacyContactsListener
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.repositories.ILegacyPlanningRepository
import org.permanent.permanent.repositories.LegacyPlanningRepositoryImpl

class LegacyStatusViewModel(application: Application) : ObservableAndroidViewModel(application){

    private val appContext = application.applicationContext
    private val onLegacyContactReady = SingleLiveEvent<List<LegacyContact>>()
    private var legacyPlanningRepository: ILegacyPlanningRepository =
        LegacyPlanningRepositoryImpl(appContext)

    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private val onYourArchivesRetrieved = MutableLiveData<List<Pair<Archive, ArchiveSteward?>>>()
    private var list: ArrayList<Pair<Archive, ArchiveSteward?>> = ArrayList()
    private val archives: MutableList<Archive> = ArrayList()
    private var index = 0

    init {
        getLegacyContact()
        getYourArchives()
    }

    private fun getLegacyContact() {
        legacyPlanningRepository.getLegacyContact(object : ILegacyContactsListener {
            override fun onSuccess(dataList: List<LegacyContact>) {
                onLegacyContactReady.value = dataList
            }

            override fun onFailed(error: String?) {
            }
        })
    }

    fun getOnLegacyContactReady(): MutableLiveData<List<LegacyContact>> = onLegacyContactReady

    fun getOnAllArchivesReady(): MutableLiveData<List<Pair<Archive, ArchiveSteward?>>> = onYourArchivesRetrieved

    private fun getYourArchives() {
        with(archiveRepository) {
            getAllArchives(object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    if (!dataList.isNullOrEmpty()) {
                        for (datum in dataList) {
                            val archive = Archive(datum.ArchiveVO)
                            if (archive.accessRole == AccessRole.OWNER) {
                                archives.add(archive)
                            }
                        }
                        archives.forEach {
                            getArchiveSteward(it)
                        }
                    }
                }

                override fun onFailed(error: String?) {
                }
            })
        }
    }

    private fun getArchiveSteward(archive: Archive) {

        legacyPlanningRepository.getArchiveSteward(archiveId = archive.id, object : ILegacyArchiveListener {
            override fun onSuccess(dataList: List<ArchiveSteward>) {
                val steward: ArchiveSteward? = dataList.firstOrNull()
                list.add(Pair(archive, steward))
                index += 1
                if(index == archives.count()) {
                    onYourArchivesRetrieved.postValue(list)
                    index = 0
                }
            }

            override fun onFailed(error: String?) {

            }
        })
    }
}