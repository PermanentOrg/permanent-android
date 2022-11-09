package org.permanent.permanent.ui.public

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
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
import org.permanent.permanent.ui.Workspace
import org.permanent.permanent.ui.activities.SignUpActivity
import org.permanent.permanent.viewmodels.PublicViewModel

class PublicFragment : PermanentBaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentPublicBinding
    private lateinit var viewModel: PublicViewModel
    private lateinit var prefsHelper: PreferencesHelper
    private var myFilesContainerFragment: MyFilesContainerFragment? = null
    private var isFileForProfileBanner = true

    private val onArchiveRetrieved = Observer<Archive> {
        setArchive(it)
    }

    private val onArchiveName = Observer<String> {
        (activity as AppCompatActivity?)?.supportActionBar?.title = it
    }

    private val onRecordSelectedObserver = Observer<Record> {
        viewModel.updateBannerOrProfilePhoto(isFileForProfileBanner, it)
    }

    private val onShowMessage = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val onShowError = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
        snackBar.show()
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
        val archiveNr: String? = arguments?.getString(ARCHIVE_NR)
        if (!archiveNr.isNullOrEmpty()) {
            if (prefsHelper.isUserLoggedIn()) {
                prefsHelper.saveDeepLinkArchiveNr("") // marks the deeplink consumed
                viewModel.getArchive(archiveNr) // a callback is set
            } else {
                prefsHelper.saveDeepLinkArchiveNr(archiveNr)
                startActivity(Intent(context, SignUpActivity::class.java))
                activity?.finish()
            }
        } else {
            setArchive(arguments?.getParcelable(ARCHIVE) ?: prefsHelper.getCurrentArchive())
        }

        return binding.root
    }

    private fun setArchive(archive: Archive) {
        viewModel.setArchive(archive)
        val isViewOnlyMode = archive.accessRole != AccessRole.OWNER && archive.accessRole != AccessRole.MANAGER
        if (isViewOnlyMode) {
            binding.fabProfileBanner.visibility = View.GONE
            binding.fabProfilePhoto.visibility = View.GONE
        }
        initViewPagerAdapter(archive, isViewOnlyMode)
    }

    private fun initViewPagerAdapter(archive: Archive, isViewOnlyMode: Boolean) {
        val viewPager = binding.vpPublic
        val tabLayout = binding.tlPublic

        val adapter = PublicViewPagerAdapter(isViewOnlyMode, archive, this)
        viewPager.adapter = adapter

        val tabArray = arrayOf(
            R.string.public_archive_tab_name,
            R.string.public_profile_tab_name
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(tabArray[position])
        }.attach()

        viewPager.setCurrentItem(1, false)
    }

    override fun onClick(view: View) {
        isFileForProfileBanner = view.id == R.id.fabProfileBanner
        myFilesContainerFragment = MyFilesContainerFragment()
        myFilesContainerFragment?.setBundleArguments(Workspace.PUBLIC_ARCHIVES)
        myFilesContainerFragment?.getOnRecordSelected()?.observe(this, onRecordSelectedObserver)
        myFilesContainerFragment?.show(parentFragmentManager, myFilesContainerFragment?.tag)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
        viewModel.getCurrentArchiveName().observe(this, onArchiveName)
        viewModel.getOnArchiveRetrieved().observe(this, onArchiveRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
        viewModel.getCurrentArchiveName().removeObserver(onArchiveName)
        myFilesContainerFragment?.getOnRecordSelected()?.removeObserver(onRecordSelectedObserver)
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
