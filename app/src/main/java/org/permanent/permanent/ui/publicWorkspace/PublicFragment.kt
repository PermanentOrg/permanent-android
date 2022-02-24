package org.permanent.permanent.ui.publicWorkspace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PublicViewModel

class PublicFragment : PermanentBaseFragment()  {

    private lateinit var binding: FragmentPublicBinding
    private lateinit var viewModel: PublicViewModel

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

        (activity as AppCompatActivity?)?.supportActionBar?.title = viewModel.getCurrentArchiveName()

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

        TabLayoutMediator(tabLayout, viewPager){tab, position->
            tab.text = getString(tabArray[position])
        }.attach()

        viewPager.setCurrentItem(1, false)
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}
