package org.permanent.permanent.ui.myFiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.permanent.databinding.FileBottomMenuBinding
import org.permanent.permanent.Constants

class FileBottomDrawerFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FileBottomMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FileBottomMenuBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.tvFileName.text = arguments?.getString(Constants.FILE_NAME)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnDownload.setOnClickListener {
            Toast.makeText(context, "Download btn pressed", Toast.LENGTH_LONG).show()
        }
        binding.btnCopy.setOnClickListener {
            Toast.makeText(context, "Copy btn pressed", Toast.LENGTH_LONG).show()
        }
    }
}