package org.permanent.permanent.ui.fileView

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.google.android.material.snackbar.Snackbar
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
                binding.etName.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) viewModel.saveChanges()
                }
                binding.etDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)
                binding.etDescription.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) viewModel.saveChanges()
                }
                binding.etDescription.setOnEditorActionListener { _, _, _ -> viewModel.saveChanges()
                    false
                }
                it.tags?.let { tags -> val mutableTags = tags.toMutableList()
                    mutableTags.sortBy { tag -> tag.name.lowercase() }
                    createChipsFor(mutableTags)
                }
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
        viewModel.saveChanges()
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
        val resultIntent = Intent()
        resultIntent.putExtra(ACTIVITY_RESULT_FILE_NAME_KEY, fileName)
        activity?.setResult(Activity.RESULT_OK, resultIntent)
    }

    private val onShowTagsEdit = Observer<Void> {
        val bundle = bundleOf(PARCELABLE_FILE_DATA_KEY to fileData)
        findNavController()
            .navigate(R.id.action_fileMetadataFragment_to_tagsEditFragment, bundle)
    }

    private val onShowMessage = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let { view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val onShowError = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let { view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
        }
        snackBar.show()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowDatePicker().observe(this, onShowDatePicker)
        viewModel.getShowLocationSearch().observe(this, onShowLocationSearch)
        viewModel.getOnFileInfoUpdated().observe(this, onFileInfoUpdated)
        viewModel.getShowTagsEdit().observe(this, onShowTagsEdit)
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowDatePicker().removeObserver(onShowDatePicker)
        viewModel.getShowLocationSearch().removeObserver(onShowLocationSearch)
        viewModel.getOnFileInfoUpdated().removeObserver(onFileInfoUpdated)
        viewModel.getShowTagsEdit().removeObserver(onShowTagsEdit)
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
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

    companion object {
        const val ACTIVITY_RESULT_FILE_NAME_KEY = "activity_result_file_name_key"
        const val ACTIVITY_RESULT_REQUEST_CODE = 1616
    }
}