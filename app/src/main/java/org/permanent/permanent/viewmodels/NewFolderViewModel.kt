package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IRecordListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class NewFolderViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentFolderName = MutableStateFlow("")
    val currentFolderName: StateFlow<String> = _currentFolderName

    val isCreateEnabled: StateFlow<Boolean> = _currentFolderName
        .map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy

    private val _onFolderCreated = MutableSharedFlow<Unit>()
    val onFolderCreated: SharedFlow<Unit> = _onFolderCreated

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage

    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun onNameChanged(name: String) {
        _currentFolderName.value = name
    }

    fun createNewFolder(parentFolderIdentifier: NavigationFolderIdentifier?) {
        if (_isBusy.value) return
        val name = _currentFolderName.value.trim()
        if (name.isEmpty() || parentFolderIdentifier == null) return

        _isBusy.value = true
        fileRepository.createFolder(parentFolderIdentifier, name, object : IRecordListener {
            override fun onSuccess(record: Record) {
                _isBusy.value = false
                viewModelScope.launch { _onFolderCreated.emit(Unit) }
            }

            override fun onFailed(error: String?) {
                _isBusy.value = false
                val message = when (error) {
                    Constants.ERROR_SERVER_ERROR ->
                        application.getString(R.string.server_error)
                    else -> error ?: return
                }
                viewModelScope.launch { _errorMessage.emit(message) }
            }
        })
    }
}
