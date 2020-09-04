package org.permanent.permanent.ui.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.permanent.databinding.FragmentMainBinding
import org.permanent.databinding.FragmentSecondaryBinding

class SecondaryFragment:Fragment() {

    private lateinit var binding: FragmentSecondaryBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSecondaryBinding.inflate(inflater,container,false)
        binding.executePendingBindings()
        binding.lifecycleOwner=this
        return binding.root
    }
}