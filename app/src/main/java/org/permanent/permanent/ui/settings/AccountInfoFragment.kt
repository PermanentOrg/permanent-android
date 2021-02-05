package org.permanent.permanent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.databinding.FragmentAccountInfoBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.AccountInfoViewModel


class AccountInfoFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentAccountInfoBinding
    private lateinit var viewModel: AccountInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(AccountInfoViewModel::class.java)
        binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    private val onError = Observer<String> {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onError)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onError)
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