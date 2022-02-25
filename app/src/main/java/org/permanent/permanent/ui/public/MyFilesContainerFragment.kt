package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.permanent.permanent.R
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.MyFilesFragment
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.viewmodels.SingleLiveEvent

const val IS_FILE_FOR_PROFILE_BANNER_KEY = "is_file_for_profile_banner_key"

class MyFilesContainerFragment : PermanentBottomSheetFragment() {

    private var myFilesFragment: MyFilesFragment? = null
    private val onProfilePhotoUpdateEvent = SingleLiveEvent<Void>()

    private val onProfilePhotoUpdateObserver = Observer<Void> {
        onProfilePhotoUpdateEvent.call()
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        myFilesFragment = MyFilesFragment()
        myFilesFragment?.getOnPhotoUpdate()?.observe(this, onProfilePhotoUpdateObserver)
        myFilesFragment?.arguments = bundleOf(
            SHOW_SCREEN_SIMPLIFIED_KEY to true,
            IS_FILE_FOR_PROFILE_BANNER_KEY to arguments?.getBoolean(IS_FILE_FOR_PROFILE_BANNER_KEY)
        )
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayoutContainer, myFilesFragment!!).commit()
    }

    fun getOnProfilePhotoUpdate(): MutableLiveData<Void> = onProfilePhotoUpdateEvent

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
        myFilesFragment?.getOnPhotoUpdate()?.removeObserver(onProfilePhotoUpdateObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}