package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.permanent.permanent.databinding.FragmentPublicArchiveBinding
import org.permanent.permanent.ui.PermanentBaseFragment

class PublicArchiveFragment : PermanentBaseFragment() {
    private var _binding: FragmentPublicArchiveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPublicArchiveBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}