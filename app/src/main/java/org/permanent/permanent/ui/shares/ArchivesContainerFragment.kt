package org.permanent.permanent.ui.shares

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
import org.permanent.permanent.ui.archives.ArchivesFragment
import org.permanent.permanent.viewmodels.SingleLiveEvent


class ArchivesContainerFragment : PermanentBottomSheetFragment() {

    private var archivesFragment: ArchivesFragment? = null
    private val onArchiveChanged = SingleLiveEvent<Void>()

    private val onCurrentArchiveChanged = Observer<Void> {
        onArchiveChanged.call()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_archives_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        archivesFragment = ArchivesFragment()
        archivesFragment?.getOnCurrentArchiveChanged()?.observe(this, onCurrentArchiveChanged)
        archivesFragment?.arguments = bundleOf(SHOW_SCREEN_SIMPLIFIED_KEY to true)
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.flArchivesContainer, archivesFragment!!).commit()
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
        archivesFragment?.getOnCurrentArchiveChanged()?.removeObserver(onCurrentArchiveChanged)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    fun getOnArchiveChanged(): MutableLiveData<Void> = onArchiveChanged
}