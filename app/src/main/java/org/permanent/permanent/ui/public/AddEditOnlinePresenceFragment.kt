package org.permanent.permanent.ui.public

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentAddEditOnlinePresenceBinding
import org.permanent.permanent.models.ProfileItem
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.viewmodels.AddEditOnlinePresenceViewModel

class AddEditOnlinePresenceFragment: PermanentBaseFragment() {
    private lateinit var viewModel: AddEditOnlinePresenceViewModel
    private lateinit var binding: FragmentAddEditOnlinePresenceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(AddEditOnlinePresenceViewModel::class.java)
        binding = FragmentAddEditOnlinePresenceBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val profileItem: ProfileItem? = arguments?.getParcelable(PublicProfileFragment.PARCELABLE_PROFILE_ITEMS_KEY)
        val isEdit: Boolean? = arguments?.getBoolean(OnlinePresenceListFragment.IS_EDIT_ONLINE_PRESENCE)
        val isAddEmail: Boolean? = arguments?.getBoolean(OnlinePresenceListFragment.IS_ADD_EMAIL)
        viewModel.displayProfileItem(profileItem, isEdit, isAddEmail)
        return binding.root
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

    private val onBackToListFragment = Observer<Void>{
        requireParentFragment().findNavController()
            .popBackStack(R.id.onlinePresenceListFragment, false)
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
        viewModel.getOnBackRequest().observe(this, onBackToListFragment)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
        viewModel.getOnBackRequest().removeObserver(onBackToListFragment)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
        context?.hideKeyboardFrom(binding.root.windowToken)
    }

}
