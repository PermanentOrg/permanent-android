package org.permanent.permanent.ui.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSharesBinding
import org.permanent.permanent.ui.PermanentBaseFragment

class SharesFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentSharesBinding
    private lateinit var viewAdapter: SharesViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSharesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewAdapter = SharesViewPagerAdapter(this)
        val viewPager = binding.viewPager
        viewPager.adapter = viewAdapter

        TabLayoutMediator(binding.tabs,viewPager) { tab , position ->
            when(position){
                Constants.POSITION_SHARED_BY_ME_FRAGMENT -> tab.text= getString(R.string.shared_by_me_tab_name_)
                Constants.POSITION_SHARED_WITH_ME_FRAGMENT -> tab.text= getString(R.string.shared_with_me_tab_name)
                else -> tab.text= getString(R.string.shared_by_me_tab_name_)
            }
        }.attach()
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
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