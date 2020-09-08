package org.permanent.permanent.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import org.permanent.R
import org.permanent.databinding.ActivityMainBinding
import org.permanent.permanent.viewmodels.MainViewModel

class MainActivity : PermanentBaseActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.viewModel = viewModel

        if (!viewModel.isWelcomeDialogSeen(getPreferences(Context.MODE_PRIVATE))) {
            showWelcomeDialog()
        }
    }

    private fun showWelcomeDialog() {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_welcome, null)

        val alert = AlertDialog.Builder(this)
            .setView(viewDialog)
            .create()

        viewDialog.ivBtnClose.setOnClickListener {
            viewModel.setWelcomeDialogSeen(getPreferences(Context.MODE_PRIVATE))
            alert.dismiss()
        }
        viewDialog.btnStartPreserving.setOnClickListener {
            viewModel.setWelcomeDialogSeen(getPreferences(Context.MODE_PRIVATE))
            alert.dismiss()
        }

        alert.show()
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}