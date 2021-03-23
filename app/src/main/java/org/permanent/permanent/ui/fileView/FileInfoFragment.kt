package org.permanent.permanent.ui.fileView

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFileInfoBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.FileInfoViewModel
import java.util.*

const val PARCELABLE_COORDINATES_KEY = "parcelable_coordinates_key"

class FileInfoFragment : PermanentBaseFragment(), OnMapReadyCallback {

    private lateinit var viewModel: FileInfoViewModel
    private lateinit var binding: FragmentFileInfoBinding
    private var mapView: MapView? = null
    private var fileData: FileData? = null
    private var coordinates: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FileInfoViewModel::class.java)
        binding = FragmentFileInfoBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        mapView = binding.mapView
        mapView?.onCreate(savedInstanceState)
        arguments?.takeIf { it.containsKey(PARCELABLE_FILE_DATA_KEY) }?.apply {
            getParcelable<FileData>(PARCELABLE_FILE_DATA_KEY)?.also {
                fileData = it
                viewModel.setFileData(it)
                if (it.latitude != -1.0 && it.longitude != -1.0)
                    mapView?.getMapAsync(this@FileInfoFragment)
            }
        }
        return binding.root
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

    private val onShowLocationSearch = Observer<Void> {
        val bundle = bundleOf(PARCELABLE_COORDINATES_KEY to coordinates)
        parentFragment?.findNavController()
            ?.navigate(R.id.action_fileMetadataFragment_to_locationSearchFragment, bundle)
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowDatePicker().observe(this, onShowDatePicker)
        viewModel.getShowLocationSearch().observe(this, onShowLocationSearch)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowDatePicker().removeObserver(onShowDatePicker)
        viewModel.getShowLocationSearch().removeObserver(onShowLocationSearch)
        viewModel.getShowMessage().removeObserver(onShowMessage)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
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

    override fun onMapReady(googleMap: GoogleMap) {
        val lat = fileData?.latitude
        val lng = fileData?.longitude
        if (lat != null && lng != null) {
            googleMap.apply {
                coordinates = LatLng(lat, lng)
                addMarker(MarkerOptions().position(coordinates!!))
                animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 9.9f))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        mapView?.onPause()
    }
}