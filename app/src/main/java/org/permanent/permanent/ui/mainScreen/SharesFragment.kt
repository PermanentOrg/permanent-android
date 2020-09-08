package org.permanent.permanent.ui.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import org.permanent.databinding.FragmentSharesBinding

class SharesFragment : Fragment() {

    private lateinit var binding: FragmentSharesBinding
    private lateinit var viewAdapter: SharesViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSharesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewAdapter = SharesViewPagerAdapter(this)
        val viewPager = binding.viewPager
        viewPager.adapter = viewAdapter

        TabLayoutMediator(binding.tabs,viewPager){tab , position ->
            when(position){
                0 -> tab.text= "Shared by me"
                1 -> tab.text= "Shared with me"
                else -> tab.text= "Shared by me"
            }
        }.attach()
    }
}