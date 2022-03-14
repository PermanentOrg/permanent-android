package org.permanent.permanent.ui.fileView

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentLocationSearchBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.LocationSearchViewModel

const val BOOLEAN_SHOULD_SCROLL_KEY = "boolean_should_scroll_key"
class LocationSearchFragment : PermanentBaseFragment(), OnMapReadyCallback, PlaceSelectionListener,
    GoogleMap.OnMapLongClickListener {

    private lateinit var menu: Menu
    private lateinit var viewModel: LocationSearchViewModel
    private lateinit var binding: FragmentLocationSearchBinding
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private var fileData: FileData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(LocationSearchViewModel::class.java)
        binding = FragmentLocationSearchBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        fileData = arguments?.getParcelable(PARCELABLE_FILE_DATA_KEY)
        fileData?.let {
            viewModel.setFileData(it)
        }
        setHasOptionsMenu(true)
        initMapFragment()
        initAutocompleteFragment()
        initDeviceBackPressCallback()
        return binding.root
    }

    private fun initAutocompleteFragment() {
        Places.initialize(requireContext(), BuildConfig.GMP_KEY)

        autocompleteFragment = AutocompleteSupportFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.cvSearch, autocompleteFragment, "autocompleteFragment")
        transaction.commit()
        childFragmentManager.executePendingTransactions()

        autocompleteFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(this@LocationSearchFragment)
    }

    override fun onPlaceSelected(place: Place) {
        place.latLng?.let {
            viewModel.onLatLngSelected(it)
            menu.findItem(R.id.doneItem).isVisible = true
        }
    }

    private fun initMapFragment() {
        val mapFragment = SupportMapFragment()
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.clMapAndSearch, mapFragment, "mapFragment")
        fragmentTransaction.commit()
        childFragmentManager.executePendingTransactions()
        mapFragment.getMapAsync(this@LocationSearchFragment)
    }

    override fun onMapReady(gMap: GoogleMap) {
        viewModel.setMap(gMap)
        gMap.setOnMapLongClickListener(this@LocationSearchFragment)
        fileData?.let {
            if (it.latitude != -1.0) viewModel.updateMarker(LatLng(it.latitude, it.longitude))
            autocompleteFragment.setText(it.completeAddress)
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        viewModel.onLatLngSelected(latLng)
        autocompleteFragment.setText("${latLng.latitude}, ${latLng.longitude}")
        menu.findItem(R.id.doneItem).isVisible = true
    }

    private fun initDeviceBackPressCallback() {
        requireActivity().onBackPressedDispatcher
            .addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateUp(viewModel.getCurrentFileData())
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_done_item, menu)
        this.menu = menu
        menu.findItem(R.id.doneItem).isVisible = false
        autocompleteFragment.requireView().findViewById<View>(R.id.places_autocomplete_clear_button)
            .setOnClickListener {
                autocompleteFragment.setText("")
                it.visibility = View.GONE
                menu.findItem(R.id.doneItem).isVisible = false
            }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.doneItem -> {
                viewModel.updateRecordLocation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onError(status: Status) {
        if (!status.isCanceled) viewModel.showMessage.value = getString(R.string.generic_error)
    }

    private val onLocationUpdated = Observer<FileData> { navigateUp(it) }

    private fun navigateUp(it: FileData?) {
        val bundle = bundleOf(
            PARCELABLE_FILE_DATA_KEY to it, BOOLEAN_SHOULD_SCROLL_KEY to true
        )
        findNavController().navigate(
            R.id.action_locationSearchFragment_to_fileMetadataFragment, bundle
        )
    }

    private val onShowMessage = Observer<String?> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.showMessage.observe(this, onShowMessage)
        viewModel.getOnRecordLocationUpdate().observe(this, onLocationUpdated)
    }

    override fun disconnectViewModelEvents() {
        viewModel.showMessage.removeObserver(onShowMessage)
        viewModel.getOnRecordLocationUpdate().removeObserver(onLocationUpdated)
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