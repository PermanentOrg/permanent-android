package org.permanent.permanent.ui.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentBiometricsBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.BiometricsViewModel

class BiometricsFragment : PermanentBaseFragment() {

    private lateinit var viewModel: BiometricsViewModel
    private lateinit var binding: FragmentBiometricsBinding

    private val onNavigateToSettings = Observer<Void> {
        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
    private val onNavigateToMainActivity = Observer<Void> {
        findNavController().navigate(R.id.action_biometricsFragment_to_mainActivity)
        activity?.finish()
    }
    private val onNavigateToLoginFragment = Observer<Void> {
        findNavController().navigate(R.id.action_biometricsFragment_to_loginFragment)
    }
    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    private val onErrorStringId = Observer<Int> { errorId ->
        val errorMessage = this.resources.getString(errorId)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BiometricsViewModel::class.java)
        viewModel.buildPromptParams(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBiometricsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    override fun connectViewModelEvents() {
        viewModel.getOnNavigateToSettings().observe(this, onNavigateToSettings)
        viewModel.getOnNavigateToMainActivity().observe(this, onNavigateToMainActivity)
        viewModel.getOnNavigateToLoginFragment().observe(this, onNavigateToLoginFragment)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
        viewModel.getErrorStringId().observe(this, onErrorStringId)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnNavigateToSettings().removeObserver(onNavigateToSettings)
        viewModel.getOnNavigateToMainActivity().removeObserver(onNavigateToMainActivity)
        viewModel.getOnNavigateToLoginFragment().removeObserver(onNavigateToLoginFragment)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
        viewModel.getErrorStringId().removeObserver(onErrorStringId)
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
