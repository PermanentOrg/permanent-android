package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicViewModel

class PublicFragment : PermanentBaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentPublicBinding
    private lateinit var viewModel: PublicViewModel
    private var myFilesContainerFragment: MyFilesContainerFragment? = null
    private var isFileForProfileBanner = true

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
        viewModel = ViewModelProvider(this).get(PublicViewModel::class.java)
        binding = FragmentPublicBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.fabProfileBanner.setOnClickListener(this)
        binding.fabProfilePhoto.setOnClickListener(this)
        (activity as AppCompatActivity?)?.supportActionBar?.title =
            viewModel.getCurrentArchiveName()

        return binding.root
    }

    private val tabArray = arrayOf(
        R.string.public_archive_tab_name,
        R.string.public_profile_tab_name
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = binding.vpPublic
        val tabLayout = binding.tlPublic

        val adapter = PublicViewPagerAdapter(this)
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
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        myFilesContainerFragment?.getOnPhotoSelected()?.removeObserver(onPhotoSelectedObserver)
    }
}
