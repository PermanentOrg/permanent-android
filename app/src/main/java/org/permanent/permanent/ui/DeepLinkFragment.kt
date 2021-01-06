package org.permanent.permanent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentDeepLinkBinding
import org.permanent.permanent.viewmodels.DeepLinkViewModel

class DeepLinkFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentDeepLinkBinding
    private lateinit var viewModel: DeepLinkViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(DeepLinkViewModel::class.java)
        binding = FragmentDeepLinkBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
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