package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
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

    val tabArray = arrayOf(
        "Public Archive",
        "Public Profile"
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = binding.vpPublic
        val tabLayout = binding.tlPublic

        val adapter = PublicViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager){tab, position->
            tab.text = tabArray[position]
        }.attach()
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}

