package org.permanent.permanent.models

import androidx.lifecycle.MutableLiveData

data class Upload(
    val displayName: String,
    val isUploading: MutableLiveData<Boolean>)