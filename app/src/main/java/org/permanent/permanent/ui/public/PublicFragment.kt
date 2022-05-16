package org.permanent.permanent.ui.public

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.PublicViewModel

class PublicFragment : PermanentBaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentPublicBinding
    private lateinit var viewModel: PublicViewModel
    private lateinit var prefsHelper: PreferencesHelper
    private var myFilesContainerFragment: MyFilesContainerFragment? = null
    private var isFileForProfileBanner = true
    private var archiveNr: String? = null
    private var isViewOnlyMode = false

    private val onArchiveName = Observer<String> {
        (activity as AppCompatActivity?)?.supportActionBar?.title = it
    }

    private val onPhotoSelectedObserver = Observer<Record> {
        viewModel.updateBannerOrProfilePhoto(isFileForProfileBanner, it)
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PublicViewModel::class.java]
        binding = FragmentPublicBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.fabProfileBanner.setOnClickListener(this)
        binding.fabProfilePhoto.setOnClickListener(this)
        activity?.toolbar?.menu?.findItem(R.id.settingsItem)?.isVisible = true

        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        val currentArchive: Archive = arguments?.getParcelable(ARCHIVE) ?: prefsHelper.getCurrentArchive()
        archiveNr = currentArchive.number
        viewModel.setArchive(currentArchive)
        isViewOnlyMode = currentArchive.accessRole != AccessRole.OWNER && currentArchive.accessRole != AccessRole.MANAGER
        if (isViewOnlyMode) {
            binding.fabProfileBanner.visibility = View.GONE
            binding.fabProfilePhoto.visibility = View.GONE
        }

        return binding.root
    }

    private val tabArray = arrayOf(
        R.string.public_archive_tab_name,
        R.string.public_profile_tab_name
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = binding.vpPublic
        val tabLayout = binding.tlPublic

        val adapter = PublicViewPagerAdapter(isViewOnlyMode, archiveNr, this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(tabArray[position])
        }.attach()

        viewPager.setCurrentItem(1, false)
    }

    override fun onClick(view: View) {
        isFileForProfileBanner = view.id == R.id.fabProfileBanner
        myFilesContainerFragment = MyFilesContainerFragment()
        myFilesContainerFragment?.getOnPhotoSelected()?.observe(this, onPhotoSelectedObserver)
        myFilesContainerFragment?.show(parentFragmentManager, myFilesContainerFragment?.tag)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getCurrentArchiveName().observe(this, onArchiveName)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getCurrentArchiveName().removeObserver(onArchiveName)
        myFilesContainerFragment?.getOnPhotoSelected()?.removeObserver(onPhotoSelectedObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val ARCHIVE = "archive"
        const val ARCHIVE_NR = "archive_nr"
    }
}
