package org.permanent.permanent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentSecuritySettingsBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.SecuritySettingsViewModel


class SecuritySettingsFragment : PermanentBaseFragment() {

    private lateinit var viewModel: SecuritySettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SecuritySettingsViewModel::class.java)
        val binding = FragmentSecuritySettingsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    private val onError = Observer<String> { error ->
        Toast.makeText(requireActivity(), error, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getErrorMessage().observe(this, onError)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getErrorMessage().removeObserver(onError)
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