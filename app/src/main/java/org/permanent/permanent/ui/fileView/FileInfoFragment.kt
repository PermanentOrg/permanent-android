package org.permanent.permanent.ui.fileView

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.material.chip.Chip
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentFileInfoBinding
import org.permanent.permanent.models.Tag
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.FileInfoViewModel
import java.util.*

class FileInfoFragment : PermanentBaseFragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var viewModel: FileInfoViewModel
    private lateinit var binding: FragmentFileInfoBinding
    private var mapView: MapView? = null
    private var fileData: FileData? = null

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
                it.tags?.let { tags -> createChipsFor(tags) }
                if (it.latitude != -1.0 && it.longitude != -1.0)
                    mapView?.getMapAsync(this@FileInfoFragment)
            }
        }
        return binding.root
    }

    private fun createChipsFor(tags: List<Tag>) {
        val chipGroup = binding.chipGroupFileTags
        for (tag in tags) {
            val chip = layoutInflater.inflate(
                R.layout.item_chip_action, chipGroup, false) as Chip
            chip.text = (tag.name)
            chip.setEnsureMinTouchTargetSize(false)
            chip.setOnClickListener { viewModel.onShowTagsEdit.call() }
            chipGroup.addView(chip)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        fileData?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            googleMap.apply {
                addMarker(MarkerOptions().position(latLng))
                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9.9f))
                if (viewModel.getIsEditable().value == true)
                    setOnMapClickListener(this@FileInfoFragment)
            }
        }
    }

    override fun onMapClick(latLng: LatLng) {
        viewModel.onShowLocationSearchRequest.call()
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
        val bundle = bundleOf(PARCELABLE_FILE_DATA_KEY to fileData)
        findNavController()
            .navigate(R.id.action_fileMetadataFragment_to_locationSearchFragment, bundle)
    }

    private val onFileInfoUpdated = Observer<String> { fileName ->
        (activity as AppCompatActivity?)?.supportActionBar?.title = fileName
    }

    private val onShowTagsEdit = Observer<Void> {
        val bundle = bundleOf(PARCELABLE_FILE_DATA_KEY to fileData)
        findNavController()
            .navigate(R.id.action_fileMetadataFragment_to_tagsEditFragment, bundle)
    }

    private val onShowMessage = Observer<String> { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowDatePicker().observe(this, onShowDatePicker)
        viewModel.getShowLocationSearch().observe(this, onShowLocationSearch)
        viewModel.getOnFileInfoUpdated().observe(this, onFileInfoUpdated)
        viewModel.getShowTagsEdit().observe(this, onShowTagsEdit)
        viewModel.getShowMessage().observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowDatePicker().removeObserver(onShowDatePicker)
        viewModel.getShowLocationSearch().removeObserver(onShowLocationSearch)
        viewModel.getOnFileInfoUpdated().removeObserver(onFileInfoUpdated)
        viewModel.getShowTagsEdit().removeObserver(onShowTagsEdit)
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