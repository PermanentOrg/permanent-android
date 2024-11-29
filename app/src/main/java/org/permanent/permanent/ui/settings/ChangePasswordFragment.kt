package org.permanent.permanent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.databinding.FragmentChangePasswordBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.viewmodels.ChangePasswordViewModel


class ChangePasswordFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewModel: ChangePasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ChangePasswordViewModel::class.java)
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    private val onMessage = Observer<String> {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
    }

    private val onPasswordChanged = Observer<Void?> {
        context?.hideKeyboardFrom(binding.root.windowToken)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onMessage)
        viewModel.getOnPasswordChanged().observe(this, onPasswordChanged)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onMessage)
        viewModel.getOnPasswordChanged().removeObserver(onPasswordChanged)
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