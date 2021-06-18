package org.permanent.permanent.ui.twoStepVerification

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityTwoStepVerificationBinding
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.ui.activities.SKIP_CODE_VERIFICATION
import org.permanent.permanent.viewmodels.TwoStepVerificationViewModel

class TwoStepVerificationActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityTwoStepVerificationBinding
    private lateinit var viewModel: TwoStepVerificationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TwoStepVerificationViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_two_step_verification)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }
    override fun onStart() {
        super.onStart()
        val skip: Boolean? = intent.extras?.getBoolean(SKIP_CODE_VERIFICATION)
        if (skip != null && skip) startPhoneVerificationFragment()
    }

    private fun startPhoneVerificationFragment() {
        binding.twoStepVerificationNavHostFragment.findNavController()
            .navigate(R.id.action_codeVerificationFragment_to_phoneVerificationFragment)
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}