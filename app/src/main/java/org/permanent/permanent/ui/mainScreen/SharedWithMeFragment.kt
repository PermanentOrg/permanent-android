package org.permanent.permanent.ui.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.permanent.databinding.FragmentTabSharedBinding

class SharedWithMeFragment: Fragment() {
    private lateinit var binding: FragmentTabSharedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTabSharedBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        //TODO remove
        binding.tvFragment.text="Shared With Me Fragment"
        return binding.root
    }
}