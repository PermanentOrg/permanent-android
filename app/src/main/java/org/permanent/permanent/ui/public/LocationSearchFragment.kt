package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
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
import kotlinx.android.synthetic.main.activity_main.*
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentLocationSearchBinding
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.LocationSearchViewModel

class LocationSearchFragment : PermanentBaseFragment(), OnMapReadyCallback, PlaceSelectionListener,
    GoogleMap.OnMapLongClickListener {

    private lateinit var viewModel: LocationSearchViewModel
    private lateinit var binding: FragmentLocationSearchBinding
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private var profileItem: ProfileItem? = null

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
        profileItem = arguments?.getParcelable(PARCELABLE_PROFILE_ITEM_KEY)
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
            activity?.toolbar?.menu?.findItem(R.id.doneItem)?.isVisible = true
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
        autocompleteFragment.requireView().findViewById<View>(R.id.places_autocomplete_clear_button)
            .setOnClickListener {
                autocompleteFragment.setText("")
                it.visibility = View.GONE
                activity?.toolbar?.menu?.findItem(R.id.doneItem)?.isVisible = false
            }
        profileItem?.let {
            val lat = it.locationVO?.latitude
            val long = it.locationVO?.longitude
            if (lat != null && long != null) viewModel.updateMarker(LatLng(lat, long))
            autocompleteFragment.setText(it.locationVO?.getUIAddress())
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        viewModel.onLatLngSelected(latLng)
        autocompleteFragment.setText("${latLng.latitude}, ${latLng.longitude}")
        activity?.toolbar?.menu?.findItem(R.id.doneItem)?.isVisible = true
    }

    private fun initDeviceBackPressCallback() {
        requireActivity().onBackPressedDispatcher
            .addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateUp()
                }
            })
    }

    fun onDoneItemClick() {
        navigateUp()
    }

    override fun onError(status: Status) {
        if (!status.isCanceled) viewModel.showMessage.value = getString(R.string.generic_error)
    }

    private val onLocationUpdated = Observer<LocnVO> {
        setFragmentResult(LOCATION_VO_REQUEST_KEY, bundleOf(LOCATION_VO_KEY to it))
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    private val onShowMessage = Observer<String?> {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.showMessage.observe(this, onShowMessage)
        viewModel.getOnLocationUpdate().observe(this, onLocationUpdated)
    }

    override fun disconnectViewModelEvents() {
        viewModel.showMessage.removeObserver(onShowMessage)
        viewModel.getOnLocationUpdate().removeObserver(onLocationUpdated)
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
        const val LOCATION_VO_REQUEST_KEY = "location_vo_request_key"
        const val LOCATION_VO_KEY = "location_vo_key"
    }
}