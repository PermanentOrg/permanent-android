package org.permanent.permanent.ui.recordMenu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordUiModel(
    val id: Int,
    val name: String,
    val isFolder: Boolean,
    val thumbUrl: String?,
    val sizeBytes: Long = 0L,
    val createdDate: String = "" // formatted like "yyyy-MM-dd HH:mm:ss" from backend
) : Parcelable