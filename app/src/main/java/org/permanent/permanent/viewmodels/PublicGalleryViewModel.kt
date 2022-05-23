package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.IDataListener
import org.permanent.permanent.network.models.Datum
import org.permanent.permanent.repositories.ArchiveRepositoryImpl
import org.permanent.permanent.repositories.IArchiveRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class PublicGalleryViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val currentArchiveThumb =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveThumbURL())
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()
    private val showError = SingleLiveEvent<String>()
    private var archiveRepository: IArchiveRepository = ArchiveRepositoryImpl(application)
    private val onYourArchivesRetrieved = MutableLiveData<List<Archive>>()
    private val onPopularArchivesRetrieved = MutableLiveData<List<Archive>>()

    init {
        getYourPublicArchives()

        val remoteConfig = setupRemoteConfig()

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            val popularArchivesString = remoteConfig.getString(POPULAR_PUBLIC_ARCHIVES_KEY)
            val gson = Gson()
            val jsonObject = gson.fromJson(popularArchivesString, JsonObject::class.java)
            val arr = jsonObject.getAsJsonArray(REMOTE_CONFIG_ARCHIVES_KEY)
            val minimizedPopularArchives = gson.fromJson(arr, Array<Archive>::class.java).asList()
            getPopularArchives(minimizedPopularArchives)
        }
    }

    private fun getYourPublicArchives() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        with(archiveRepository) {
            getAllArchives(object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    if (!dataList.isNullOrEmpty()) {
                        val archives: MutableList<Archive> = ArrayList()
                        for (datum in dataList) {
                            val archive = Archive(datum.ArchiveVO)
                            if (archive.isPublic == 1) {
                                archives.add(archive)
                            }
                        }
                        onYourArchivesRetrieved.value = archives
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
        }
    }

    private fun setupRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // FOR DEVELOPMENT PURPOSE ONLY
        // The default minimum fetch interval is 12 hours
//        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = 30
//        }
//        remoteConfig.setConfigSettingsAsync(configSettings)
        return remoteConfig
    }

    fun getPopularArchives(minimizedPopularArchives: List<Archive>) {
        val archiveNumbers = minimizedPopularArchives.map { it.number }
        isBusy.value = true
        with(archiveRepository) {
            getArchivesByNr(archiveNumbers, object : IDataListener {
                override fun onSuccess(dataList: List<Datum>?) {
                    isBusy.value = false
                    if (!dataList.isNullOrEmpty()) {
                        val archives: MutableList<Archive> = ArrayList()
                        for (datum in dataList) {
                            val archive = Archive(datum.ArchiveVO)
                            archive.isPopular = true
                            archives.add(archive)
                        }
                        onPopularArchivesRetrieved.value = archives
                    }
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    error?.let { showError.value = it }
                }
            })
        }
    }

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy
    fun getShowMessage(): MutableLiveData<String> = showMessage
    fun getShowError(): MutableLiveData<String> = showError

    fun getOnYourArchivesRetrieved(): LiveData<List<Archive>> = onYourArchivesRetrieved
    fun getOnPopularArchivesRetrieved(): LiveData<List<Archive>> = onPopularArchivesRetrieved

    fun sharePublicArchive(archive: Archive) {
        val sharableLink = BuildConfig.BASE_URL + "p/archive/" + archive.number +
                "/profile"
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(
            appContext.getString(R.string.share_link_share_link_title), sharableLink
        )
        clipboard.setPrimaryClip(clip)
        showMessage.value = appContext.getString(R.string.share_link_link_copied)
    }

    companion object {
        private const val POPULAR_PUBLIC_ARCHIVES_KEY = "popular_public_archives_android"
        private const val REMOTE_CONFIG_ARCHIVES_KEY = "archives"
    }
}