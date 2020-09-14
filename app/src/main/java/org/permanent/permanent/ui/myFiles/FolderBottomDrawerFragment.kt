package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.databinding.FolderBottomMenuBinding
import org.permanent.permanent.Constants

class FolderBottomDrawerFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FolderBottomMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FolderBottomMenuBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.tvFolderName.text = arguments?.getString(Constants.FOLDER_NAME)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnEdit.setOnClickListener {
            Toast.makeText(context, "Edit btn pressed", Toast.LENGTH_LONG).show()
        }
        binding.btnShare.setOnClickListener {
            Toast.makeText(context, "Share btn pressed", Toast.LENGTH_LONG).show()
        }
    }
}