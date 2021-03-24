package org.permanent.permanent.ui.fileView

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentLocationSearchBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.LocationSearchViewModel

class LocationSearchFragment : PermanentBaseFragment(), OnMapReadyCallback, PlaceSelectionListener {

    private lateinit var viewModel: LocationSearchViewModel
    private lateinit var binding: FragmentLocationSearchBinding
    private var fileData: FileData? = null
    private var coordinates: LatLng? = null
    private var googleMap: GoogleMap? = null

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
        fileData?.let { if (it.latitude != -1.0) coordinates = LatLng(it.latitude, it.longitude) }
        setHasOptionsMenu(true)
        initMapFragment()
        initAutocompleteFragment()
        return binding.root
    }

    private fun initAutocompleteFragment() {
        Places.initialize(requireContext(), Constants.API_KEY_MAPS)

        val autocompleteFragment = AutocompleteSupportFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.cvSearch, autocompleteFragment, "autocompleteFragment")
        transaction.commit()
        childFragmentManager.executePendingTransactions()

        autocompleteFragment.setPlaceFields(listOf(Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(this@LocationSearchFragment)
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
        googleMap = gMap
        if (coordinates != null) {
            gMap.apply {
                addMarker(MarkerOptions().position(coordinates!!))
                animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16.0f))
            }
        }
    }

    override fun onPlaceSelected(place: Place) {
        googleMap?.apply {
            place.latLng?.let { latLng ->
                clear()
                addMarker(MarkerOptions().position(latLng))
                moveCamera(CameraUpdateFactory.newLatLng(latLng))
                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
                viewModel.requestLocation(latLng)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_location_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.doneItem -> {
                fileData?.let { viewModel.updateRecordLocation(it) }
                // TODO: close this fragment
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onError(status: Status) {
        if (!status.isCanceled) viewModel.showMessage.value = getString(R.string.generic_error)
    }

    private val onShowMessage = Observer<String> { message ->
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
    }

    override fun connectViewModelEvents() {
        viewModel.showMessage.observe(this, onShowMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.showMessage.removeObserver(onShowMessage)
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