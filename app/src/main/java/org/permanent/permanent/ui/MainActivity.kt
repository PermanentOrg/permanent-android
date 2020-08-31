package org.permanent.permanent.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.permanent.R
import org.permanent.databinding.ActivityMainBinding

class MainActivity : PermanentBaseActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner=this
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}