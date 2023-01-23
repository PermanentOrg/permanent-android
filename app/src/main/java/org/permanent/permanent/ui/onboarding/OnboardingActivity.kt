package org.permanent.permanent.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.permanent.permanent.R
import org.permanent.permanent.START_DESTINATION_FRAGMENT_ID_KEY
import org.permanent.permanent.databinding.ActivityOnboardingBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.viewmodels.OnboardingViewModel


class OnboardingActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        viewModel = ViewModelProvider(this)[OnboardingViewModel::class.java]
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        binding.btnOnboarding.setOnClickListener {
            if (viewModel.snapPosition.value == viewAdapter.itemCount - 1) {
                onOnboardingCompleted()
            } else {
                viewModel.snapPosition.value = viewModel.snapPosition.value?.plus(1)
                recyclerView.smoothScrollToPosition(viewModel.snapPosition.value!!)
            }
        }
    }

    private fun setupRecyclerView() {
        viewAdapter = OnboardingPageAdapter()
        val positionChangeListener = object : OnSnapPositionChangeListener {
            override fun onSnapPositionChange(position: Int) {
                viewModel.snapPosition.value = position
            }
        }
        recyclerView = binding.rvOnboarding.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            attachSnapHelperWithListener(
                PagerSnapHelper(), onSnapPositionChangeListener = positionChangeListener
            )
        }
        binding.indicator.attachToRecyclerView(recyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_onboarding, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.skip -> {
                onOnboardingCompleted()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onOnboardingCompleted() {
        viewModel.setOnboardingCompleted(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))
        startSignUpFragment()
    }

    private fun startSignUpFragment() {
        val intent = Intent(this@OnboardingActivity, LoginActivity::class.java)
        intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.signUpFragment)
        startActivity(intent)
        finish()
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