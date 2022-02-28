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
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.myFiles.MyFilesFragment
import org.permanent.permanent.ui.shares.SHOW_SCREEN_SIMPLIFIED_KEY
import org.permanent.permanent.viewmodels.SingleLiveEvent

class MyFilesContainerFragment : PermanentBottomSheetFragment() {

    private var myFilesFragment: MyFilesFragment? = null
    private val onPhotoSelectedEvent = SingleLiveEvent<Record>()

    private val onPhotoSelectedObserver = Observer<Record> {
        onPhotoSelectedEvent.value = it
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
        myFilesFragment?.getOnPhotoSelected()?.observe(this, onPhotoSelectedObserver)
        myFilesFragment?.arguments = bundleOf(SHOW_SCREEN_SIMPLIFIED_KEY to true)
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayoutContainer, myFilesFragment!!).commit()
    }

    fun getOnPhotoSelected(): MutableLiveData<Record> = onPhotoSelectedEvent

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
        myFilesFragment?.getOnPhotoSelected()?.removeObserver(onPhotoSelectedObserver)
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