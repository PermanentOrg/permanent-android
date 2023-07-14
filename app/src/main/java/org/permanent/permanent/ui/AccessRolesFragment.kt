package org.permanent.permanent.ui

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentAccessRolesBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Share
import org.permanent.permanent.network.models.Shareby_urlVO
import org.permanent.permanent.ui.shareManagement.ShareManagementFragment
import org.permanent.permanent.viewmodels.AccessRolesViewModel
import org.permanent.permanent.viewmodels.SingleLiveEvent

class AccessRolesFragment : PermanentBottomSheetFragment() {
    private lateinit var binding: FragmentAccessRolesBinding
    private lateinit var viewModel: AccessRolesViewModel
    private val onAccessRoleUpdated = SingleLiveEvent<AccessRole>()

    fun setBundleArguments(
        shareByUrlVo: Shareby_urlVO,
    ) {
        val bundle = Bundle()
        bundle.putParcelable(ShareManagementFragment.SHARE_BY_URL_VO_KEY, shareByUrlVo)
        this.arguments = bundle
    }

    fun setBundleArguments(
        share: Share,
    ) {
        val bundle = Bundle()
        bundle.putParcelable(ShareManagementFragment.PARCELABLE_SHARE_KEY, share)
        this.arguments = bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AccessRolesViewModel::class.java]
        binding = FragmentAccessRolesBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setShareLink(arguments?.getParcelable(ShareManagementFragment.SHARE_BY_URL_VO_KEY))
        viewModel.setShare(arguments?.getParcelable(ShareManagementFragment.PARCELABLE_SHARE_KEY))
        initCurrentAccessRole()

        return binding.root
    }

    private fun initCurrentAccessRole() {
        when (viewModel.getCheckedAccessRole().value) {
            AccessRole.CONTRIBUTOR -> binding.radioGroup.check(R.id.rbContributor)
            AccessRole.EDITOR -> binding.radioGroup.check(R.id.rbEditor)
            AccessRole.CURATOR -> binding.radioGroup.check(R.id.rbCurator)
            AccessRole.OWNER -> binding.radioGroup.check(R.id.rbOwner)
            else -> {
                binding.radioGroup.check(R.id.rbViewer)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val dialogc = dialog as BottomSheetDialog
            val bottomSheet =
                dialogc.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as FrameLayout)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return bottomSheetDialog
    }

    private val showSnackbarSuccess = Observer<String> { message ->
        dialog?.window?.decorView?.let {
            val snackBar = Snackbar.make(it, message, Snackbar.LENGTH_LONG)
            val view: View = snackBar.view
            context?.let {
                view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepGreen))
                snackBar.setTextColor(ContextCompat.getColor(it, R.color.paleGreen))
            }
            val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
            snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
            snackBar.show()
        }
    }

    private val showSnackbar = Observer<String> { message ->
        dialog?.window?.decorView?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private val onCloseSheetObserver = Observer<Void?> {
        dismiss()
    }

    private val onAccessRoleUpdatedObserver = Observer<AccessRole?> {
        onAccessRoleUpdated.value = it
        dismiss()
    }

    private val showAccessRolesDocObserver = Observer<Void?> {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(BuildConfig.ACCESS_ROLES_URL)
        startActivity(intent)
    }

    fun getOnAccessRoleUpdated(): MutableLiveData<AccessRole> = onAccessRoleUpdated

    override fun connectViewModelEvents() {
        viewModel.getShowAccessRolesDocumentation().observe(this, showAccessRolesDocObserver)
        viewModel.getOnAccessRoleUpdated().observe(this, onAccessRoleUpdatedObserver)
        viewModel.getOnCloseSheetRequest().observe(this, onCloseSheetObserver)
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowAccessRolesDocumentation().removeObserver(showAccessRolesDocObserver)
        viewModel.getOnAccessRoleUpdated().removeObserver(onAccessRoleUpdatedObserver)
        viewModel.getOnCloseSheetRequest().removeObserver(onCloseSheetObserver)
        viewModel.getShowSnackbar().observe(this, showSnackbar)
        viewModel.getShowSnackbarSuccess().observe(this, showSnackbarSuccess)
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