package org.permanent.permanent.ui.settings

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.databinding.DialogDeleteAccountBinding
import org.permanent.permanent.databinding.FragmentAccountBinding
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.login.AuthenticationActivity
import org.permanent.permanent.ui.login.START_DESTINATION_PAGE_VALUE_KEY
import org.permanent.permanent.ui.login.compose.AuthPage
import org.permanent.permanent.viewmodels.AccountViewModel
import org.permanent.permanent.viewmodels.DeleteAccountViewModel


class AccountFragment : PermanentBaseFragment() {

    private lateinit var dialog: Dialog
    private lateinit var binding: FragmentAccountBinding
    private lateinit var dialogBinding: DialogDeleteAccountBinding

    private lateinit var viewModel: AccountViewModel
    private lateinit var dialogViewModel: DeleteAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        viewModel.sendEvent(AccountEventAction.OPEN_LOGIN_INFO)
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        dialog = Dialog(requireActivity())
        dialogViewModel = ViewModelProvider(this).get(DeleteAccountViewModel::class.java)
        dialogBinding = DialogDeleteAccountBinding.inflate(inflater, container, false)
        dialogBinding.viewModel = dialogViewModel
        dialog.setContentView(dialogBinding.root)
        return binding.root
    }

    private val onError = Observer<String?> {
        it?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private val onToast = Observer<String> {
        Toast.makeText(requireActivity(), it, Toast.LENGTH_LONG).show()
    }

    private val onShowDeleteAccountDialog = Observer<Void?> {
        dialog.show()
        val window: Window = dialog.window!!
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private val onAccountDeleted = Observer<Void?> {
        dialog.dismiss()
        val currentActivity = PermanentApplication.instance.currentActivity
        val intent = Intent(currentActivity, AuthenticationActivity::class.java)
        intent.putExtra(START_DESTINATION_PAGE_VALUE_KEY, AuthPage.SIGN_UP.value)
        currentActivity?.startActivity(intent)
        currentActivity?.finish()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onError)
        dialogViewModel.getShowMessage().observe(this, onToast)
        viewModel.getOnShowDeleteAccountDialog().observe(this, onShowDeleteAccountDialog)
        dialogViewModel.getOnAccountDeleted().observe(this, onAccountDeleted)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onError)
        dialogViewModel.getShowMessage().removeObserver(onToast)
        viewModel.getOnShowDeleteAccountDialog().removeObserver(onShowDeleteAccountDialog)
        dialogViewModel.getOnAccountDeleted().removeObserver(onAccountDeleted)
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