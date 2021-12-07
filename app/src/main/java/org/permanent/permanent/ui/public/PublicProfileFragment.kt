package org.permanent.permanent.ui.public

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.permanent.permanent.databinding.FragmentPublicProfileBinding
import org.permanent.permanent.ui.PermanentBaseFragment

class PublicProfileFragment : PermanentBaseFragment() {
    private var _binding: FragmentPublicProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPublicProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}