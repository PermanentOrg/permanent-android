package org.permanent.permanent.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
    private lateinit var googlePayLauncher: GooglePayLauncher
    private var isGooglePayReady: Boolean = false

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

        binding.tlAmount.addTab(binding.tlAmount.newTab().setText(DONATION_AMOUNT_10_VALUE))
        binding.tlAmount.addTab(binding.tlAmount.newTab().setText(DONATION_AMOUNT_20_VALUE))
        binding.tlAmount.addTab(binding.tlAmount.newTab().setText(DONATION_AMOUNT_50_VALUE))
        binding.tlAmount.addOnTabSelectedListener(this)

        PaymentConfiguration.init(requireContext(), BuildConfig.PUBLISHABLE_KEY)

        googlePayLauncher = GooglePayLauncher(
            this,
            config = GooglePayLauncher.Config(
                environment = GooglePayEnvironment.Production,
                merchantCountryCode = MERCHANT_COUNTRY_CODE,
                merchantName = MERCHANT_NAME
            ),
            readyCallback = ::onGooglePayReady,
            resultCallback = ::onGooglePayResult
        )
        binding.btnEndow.setOnClickListener {
            if (!isGooglePayReady) showDialogThatNavigatesToGoogleWallet()
            else viewModel.getPaymentIntent()
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

    private val onPaymentIntentObserver = Observer<String> {
        googlePayLauncher.presentForPaymentIntent(it)
    }

    private val onErrorObserver = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message.toString(), Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
        snackBar.show()
    }

    private fun onGooglePayReady(isReady: Boolean) {
        isGooglePayReady = isReady
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            GooglePayLauncher.Result.Completed -> {
                showDialog(
                    R.string.storage_donation_successful_title,
                    getString(R.string.storage_purchase_successful_text)
                )
            }
            GooglePayLauncher.Result.Canceled -> {
                Log.d(TAG, "User canceled the operation")
            }
            is GooglePayLauncher.Result.Failed -> {
                showDialog(R.string.storage_purchase_failed_title, result.error.message)
            }
        }
    }

    private fun showDialog(title: Int, text: String?) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialog.setTitle(title)
        alertDialog.setMessage(text)
        alertDialog.setPositiveButton(
            R.string.button_ok
        ) { _, _ ->
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun showDialogThatNavigatesToGoogleWallet() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialog.setTitle(R.string.storage_donation_no_card_title)
        alertDialog.setMessage(R.string.storage_donation_no_card_text)
        alertDialog.setPositiveButton(
            R.string.button_go_to_wallet
        ) { _, _ ->
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$GOOGLE_WALLET_APP_ID")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$GOOGLE_WALLET_APP_ID")
                    )
                )
            }
        }
        alertDialog.setNegativeButton(R.string.button_cancel) { _, _ -> }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnPaymentIntentRetrieved().observe(this, onPaymentIntentObserver)
        viewModel.getOnError().observe(this, onErrorObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnPaymentIntentRetrieved().removeObserver(onPaymentIntentObserver)
        viewModel.getOnError().removeObserver(onErrorObserver)
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
        private const val GOOGLE_WALLET_APP_ID = "com.google.android.apps.walletnfcrel"
    }
}