package org.permanent.permanent.ui.twoStepVerification

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.permanent.R
import org.permanent.databinding.ActivityTwoStepVerificationBinding
import org.permanent.permanent.ui.PermanentBaseActivity

class TwoStepVerificationActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityTwoStepVerificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_two_step_verification)
        binding.executePendingBindings()
        binding.lifecycleOwner=this
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}