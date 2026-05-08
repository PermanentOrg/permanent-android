package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.permanent.permanent.models.Record
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.FileRepositoryImpl
import org.permanent.permanent.repositories.IFileRepository

class RenameRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentRecordName = MutableStateFlow("")
    val currentRecordName: StateFlow<String> = _currentRecordName

    private var originalRecordName = ""

    val isRenameEnabled: StateFlow<Boolean> = _currentRecordName
        .map { it.isNotBlank() && it != originalRecordName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy

    private val _onRecordRenamed = MutableSharedFlow<Unit>()
    val onRecordRenamed: SharedFlow<Unit> = _onRecordRenamed

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage: SharedFlow<String> = _successMessage

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage

    private var fileRepository: IFileRepository = FileRepositoryImpl(application)

    fun setRecordName(displayName: String?) {
        val name = displayName ?: ""
        originalRecordName = name
        _currentRecordName.value = name
    }

    fun onNameChanged(name: String) {
        _currentRecordName.value = name
    }

    fun renameRecord(record: Record) {
        if (_isBusy.value) return
        val name = _currentRecordName.value.trim()
        if (name.isEmpty()) return

        _isBusy.value = true
        fileRepository.updateRecord(record, name, object : IResponseListener {
            override fun onSuccess(message: String?) {
                _isBusy.value = false
                viewModelScope.launch { _onRecordRenamed.emit(Unit) }
            }

            override fun onFailed(error: String?) {
                _isBusy.value = false
                error?.let { viewModelScope.launch { _errorMessage.emit(it) } }
            }
        })
    }
}
