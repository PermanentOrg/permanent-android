package org.permanent.permanent.ui.myFiles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_menu_layout.*
import org.permanent.R
import org.permanent.databinding.BottomMenuLayoutBinding
import org.permanent.databinding.FragmentMainBinding

class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomMenuLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomMenuLayoutBinding.inflate(inflater, container, false)
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
        binding.btnDelete.setOnClickListener {
            Toast.makeText(context, "Copy btn pressed", Toast.LENGTH_LONG).show()
        }
    }

}