package org.permanent.permanent.ui.public

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentPublicGalleryBinding
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.public.PublicFragment.Companion.ARCHIVE
import org.permanent.permanent.viewmodels.PublicGalleryViewModel

class PublicGalleryFragment : PermanentBaseFragment(), PublicArchiveListener {
    private lateinit var viewModel: PublicGalleryViewModel
    private lateinit var binding: FragmentPublicGalleryBinding
    private lateinit var yourArchivesAdapter: PublicArchiveAdapter
    private lateinit var popularArchivesAdapter: PublicArchiveAdapter
    private lateinit var yourArchivesRecyclerView: RecyclerView
    private lateinit var popularArchivesRecyclerView: RecyclerView

    private val onYourArchivesRetrieved = Observer<List<Archive>> {
        yourArchivesAdapter.set(it as MutableList<Archive>)
    }

    private val onPopularArchivesRetrieved = Observer<List<Archive>> {
        popularArchivesAdapter.set(it as MutableList<Archive>)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PublicGalleryViewModel::class.java]
        binding = FragmentPublicGalleryBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initYourArchivesRecyclerView(binding.rvYourPublicArchives)
        initPopularArchivesRecyclerView(binding.rvPopularPublicArchives)
        val remoteConfig = setupRemoteConfig()

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            val popularArchivesString = remoteConfig.getString(POPULAR_PUBLIC_ARCHIVES_KEY)
            val gson = Gson()
            val jsonObject = gson.fromJson(popularArchivesString, JsonObject::class.java)
            val arr = jsonObject.getAsJsonArray(REMOTE_CONFIG_ARCHIVES_KEY)
            val minimizedPopularArchives = gson.fromJson(arr, Array<Archive>::class.java).asList()
            viewModel.getPopularArchives(minimizedPopularArchives)
        }

        return binding.root
    }

    private fun initYourArchivesRecyclerView(rvYourPublicArchives: RecyclerView) {
        yourArchivesRecyclerView = rvYourPublicArchives
        yourArchivesAdapter = PublicArchiveAdapter(this)
        yourArchivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = yourArchivesAdapter
        }
    }

    private fun initPopularArchivesRecyclerView(rvPopularPublicArchives: RecyclerView) {
        popularArchivesRecyclerView = rvPopularPublicArchives
        popularArchivesAdapter = PublicArchiveAdapter(this)
        popularArchivesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = popularArchivesAdapter
        }
    }

    private fun setupRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // FOR DEVELOPMENT PURPOSE ONLY
        // The default minimum fetch interval is 12 hours
//        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = 30
//        }
//        remoteConfig.setConfigSettingsAsync(configSettings)
        return remoteConfig
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
        viewModel.getOnYourArchivesRetrieved().observe(this, onYourArchivesRetrieved)
        viewModel.getOnPopularArchivesRetrieved().observe(this, onPopularArchivesRetrieved)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
        viewModel.getOnYourArchivesRetrieved().removeObserver(onYourArchivesRetrieved)
        viewModel.getOnPopularArchivesRetrieved().removeObserver(onPopularArchivesRetrieved)
    }

    override fun onArchiveClick(archive: Archive) {
        val bundle = bundleOf(ARCHIVE to archive)
        requireParentFragment().findNavController()
            .navigate(R.id.action_publicGalleryFragment_to_publicFragment, bundle)
    }

    override fun onShareClick(archive: Archive) {
        viewModel.sharePublicArchive(archive)
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
        private const val POPULAR_PUBLIC_ARCHIVES_KEY = "popular_public_archives_android"
        private const val REMOTE_CONFIG_ARCHIVES_KEY = "archives"
    }
}