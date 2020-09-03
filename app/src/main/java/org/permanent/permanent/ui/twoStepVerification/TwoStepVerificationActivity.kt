package org.permanent.permanent.ui.twoStepVerification

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.permanent.R
import org.permanent.databinding.ActivityTwoStepVerificationBinding
import org.permanent.permanent.ui.PermanentBaseActivity

class TwoStepVerificationActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityTwoStepVerificationBinding
    private lateinit var viewModel: TwoStepVerificationActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TwoStepVerificationActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_two_step_verification)
        binding.executePendingBindings()
        binding.lifecycleOwner=this
        binding.viewModel = viewModel
    }

    override fun connectViewModelEvents() {

    }

    override fun disconnectViewModelEvents() {

    }
}