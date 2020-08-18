package org.permanent.permanent.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.permanent.R
import org.permanent.databinding.ActivityOnboardingBinding
import org.permanent.permanent.ui.LoginActivity
import org.permanent.permanent.ui.PermanentBaseActivity
import org.permanent.permanent.viewmodels.OnboardingViewModel


class OnboardingActivity : PermanentBaseActivity() {

    private var currentSnapPosition = MutableLiveData(0)
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        viewModel = ViewModelProvider(this).get(OnboardingViewModel::class.java)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.snapPosition = currentSnapPosition

        if (viewModel.isOnboardingCompleted(getPreferences(Context.MODE_PRIVATE))) {
            startLoginActivity()
        }

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        binding.btnOnboarding.setOnClickListener {
            if(currentSnapPosition.value == viewAdapter.itemCount - 1) {
                startLoginActivity()
                viewModel.setOnboardingCompleted(getPreferences(Context.MODE_PRIVATE))
            } else {
                currentSnapPosition.value = currentSnapPosition.value?.plus(1)
                recyclerView.smoothScrollToPosition(currentSnapPosition.value!!)
            }
        }
    }

    private fun setupRecyclerView() {
        viewManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewAdapter = OnboardingPageAdapter()
        val positionChangeListener = object : OnSnapPositionChangeListener {
            override fun onSnapPositionChange(position: Int) {
                currentSnapPosition.value = position
            }
        }
        recyclerView = binding.rvOnboarding.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            attachSnapHelperWithListener(
                PagerSnapHelper(), onSnapPositionChangeListener = positionChangeListener
            )
        }
        binding.indicator.attachToRecyclerView(recyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.skip -> {
                startLoginActivity()
                viewModel.setOnboardingCompleted(getPreferences(Context.MODE_PRIVATE))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startLoginActivity() {
        startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
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