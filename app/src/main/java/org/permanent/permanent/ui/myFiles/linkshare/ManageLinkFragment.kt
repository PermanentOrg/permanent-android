package org.permanent.permanent.ui.myFiles.linkshare

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentManageLinkBinding
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.ShareByUrl
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.myFiles.PARCELABLE_RECORD_KEY
import org.permanent.permanent.viewmodels.ManageLinkViewModel
import java.util.*


class ManageLinkFragment : PermanentBaseFragment() {

    private lateinit var viewModel: ManageLinkViewModel
    private lateinit var binding: FragmentManageLinkBinding
    private var record: Record? = null
    private var shareByUrl: ShareByUrl? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ManageLinkViewModel::class.java)
        binding = FragmentManageLinkBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        record = arguments?.getParcelable(PARCELABLE_RECORD_KEY)
        shareByUrl = arguments?.getParcelable(PARCELABLE_SHARE_KEY)
        record?.let {
            viewModel.setRecord(it)
        }
        shareByUrl?.let {
            viewModel.setShareByUrl(it)
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Device's back press
        requireActivity().onBackPressedDispatcher
            .addCallback(this , object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(R.id.shareLinkFragment, true)
                }
            })
    }

    private val onShowDatePickerRequest = Observer<Void> {
        context?.let { context ->
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(context, viewModel, year, month, day).show()
        }
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnShowDatePickerRequest().observe(this, onShowDatePickerRequest)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnShowDatePickerRequest().removeObserver(onShowDatePickerRequest)
        viewModel.getShowMessage().removeObserver(onShowMessage)
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