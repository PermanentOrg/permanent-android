package org.permanent.permanent.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.stripe.android.PaymentConfiguration
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentStorageBinding
import org.permanent.permanent.viewmodels.StorageViewModel
import org.permanent.permanent.viewmodels.StorageViewModel.Companion.DONATION_AMOUNT_10_VALUE
import org.permanent.permanent.viewmodels.StorageViewModel.Companion.DONATION_AMOUNT_20_VALUE
import org.permanent.permanent.viewmodels.StorageViewModel.Companion.DONATION_AMOUNT_50_VALUE

class StorageFragment : PermanentBaseFragment(), TabLayout.OnTabSelectedListener {

    private lateinit var binding: FragmentStorageBinding
    private lateinit var viewModel: StorageViewModel
    private lateinit var googlePayButton: RelativeLayout
    private lateinit var googlePayLauncher: GooglePayLauncher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[StorageViewModel::class.java]
        binding = FragmentStorageBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        googlePayButton = binding.buttonLayout

        binding.tlAmount.addTab(binding.tlAmount.newTab().setText(DONATION_AMOUNT_10_VALUE))
        binding.tlAmount.addTab(binding.tlAmount.newTab().setText(DONATION_AMOUNT_20_VALUE))
        binding.tlAmount.addTab(binding.tlAmount.newTab().setText(DONATION_AMOUNT_50_VALUE))
        binding.tlAmount.addOnTabSelectedListener(this)

        PaymentConfiguration.init(requireContext(), BuildConfig.PUBLISHABLE_KEY)

        googlePayLauncher = GooglePayLauncher(
            this,
            config = GooglePayLauncher.Config(
                environment = GooglePayEnvironment.Test,
                merchantCountryCode = MERCHANT_COUNTRY_CODE,
                merchantName = MERCHANT_NAME
            ),
            readyCallback = ::onGooglePayReady,
            resultCallback = ::onGooglePayResult
        )
        binding.buttonLayout.setOnClickListener {
            viewModel.getClientSecret()
        }

        return binding.root
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (binding.tlAmount.selectedTabPosition) {
            0 -> {
                viewModel.amount.value = DONATION_AMOUNT_10_VALUE.removePrefix("$")
            }
            1 -> {
                viewModel.amount.value = DONATION_AMOUNT_20_VALUE.removePrefix("$")
            }
            else -> {
                viewModel.amount.value = DONATION_AMOUNT_50_VALUE.removePrefix("$")
            }
        }
        viewModel.gbEndowed.value = getString(
            R.string.storage_gb_endowed,
            (viewModel.amount.value?.toInt()?.div(10)).toString()
        )
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    private val onClientSecretObserver = Observer<String> {
        googlePayLauncher.presentForPaymentIntent(it)
    }

    private val onMessageObserver = Observer<String> {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
    }

    private fun onGooglePayReady(isReady: Boolean) {
        googlePayButton.isEnabled = isReady
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            GooglePayLauncher.Result.Completed -> {
                showDialog(
                    R.string.storage_donation_successful_title,
                    getString(R.string.storage_donation_successful_text)
                )
            }
            GooglePayLauncher.Result.Canceled -> {
                Log.d(TAG, "User canceled the operation")
            }
            is GooglePayLauncher.Result.Failed -> {
                showDialog(R.string.storage_donation_failed_title, result.error.message)
            }
        }
    }

    private fun showDialog(title: Int, text: String?) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialog.setTitle(title)
//        alertDialog.setMessage(text)
        alertDialog.setPositiveButton(
            R.string.ok_button
        ) { _, _ ->
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnClientSecretRetrieved().observe(this, onClientSecretObserver)
        viewModel.getOnMessage().observe(this, onMessageObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnClientSecretRetrieved().removeObserver(onClientSecretObserver)
        viewModel.getOnMessage().removeObserver(onMessageObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        private val TAG = StorageFragment::class.java.simpleName
        private const val MERCHANT_COUNTRY_CODE = "US"
        private const val MERCHANT_NAME = "Permanent.org"
    }
}