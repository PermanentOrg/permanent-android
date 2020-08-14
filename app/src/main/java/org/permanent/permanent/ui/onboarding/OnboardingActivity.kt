package org.permanent.permanent.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import org.permanent.R
import org.permanent.databinding.ActivityOnboardingBinding
import org.permanent.permanent.ui.LoginActivity


class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        val sharedPref = getPreferences(Context.MODE_PRIVATE)

        val onboardingCompleted = sharedPref.getBoolean(getString(R.string.onboarding_completed), false)

        if (onboardingCompleted) {
            startSignUpActivity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.skip -> {
                startSignUpActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startSignUpActivity() {
        startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
        finish()
    }
}