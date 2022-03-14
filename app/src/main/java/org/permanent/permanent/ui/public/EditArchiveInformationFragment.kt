package org.permanent.permanent.ui.public


import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.MapView
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentEditArchiveInformationBinding
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.ui.public.PublicProfileFragment.Companion.PARCELABLE_PROFILE_ITEM_KEY
import org.permanent.permanent.viewmodels.EditArchiveInformationViewModel
import java.util.*

class EditArchiveInformationFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentEditArchiveInformationBinding
    private lateinit var viewModel: EditArchiveInformationViewModel
    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(EditArchiveInformationViewModel::class.java)
        binding = FragmentEditArchiveInformationBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)

        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(
            R.string.edit_archive_information_label,
            viewModel.getCurrentArchiveType().toTitleCase()
        )

        arguments?.getParcelableArrayList<ProfileItem>(PublicProfileFragment.PARCELABLE_PROFILE_ITEM_LIST_KEY)
            ?.let { viewModel.displayProfileItems(it) }

        if (viewModel.getLocation().value?.isNotEmpty() == true)
            mapView?.getMapAsync(viewModel)

        setFragmentResultListener(LocationSearchFragment.LOCATION_VO_REQUEST_KEY) { _, bundle ->
            val locnVO = bundle.getParcelable<LocnVO>(LocationSearchFragment.LOCATION_VO_KEY)
            locnVO?.let { viewModel.onLocationUpdated(it) }
        }
        return binding.root
    }

    private val onShowMessage = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val onShowError = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
        snackBar.show()
    }

    private val onShowDatePicker = Observer<Void> {
        context?.let { context ->
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(context, viewModel, year, month, day).show()
        }
    }

    private val onShowLocationSearch = Observer<ProfileItem?> {
        val bundle = bundleOf(PARCELABLE_PROFILE_ITEM_KEY to it)
        findNavController()
            .navigate(R.id.action_editArchiveInformationFragment_to_locationSearchFragment, bundle)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
        viewModel.getShowDatePicker().observe(this, onShowDatePicker)
        viewModel.getShowLocationSearch().observe(this, onShowLocationSearch)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
        viewModel.getShowDatePicker().removeObserver(onShowDatePicker)
        viewModel.getShowLocationSearch().removeObserver(onShowLocationSearch)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        context?.hideKeyboardFrom(binding.root.windowToken)
        mapView?.onPause()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}
