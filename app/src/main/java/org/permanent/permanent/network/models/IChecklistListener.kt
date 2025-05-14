package org.permanent.permanent.network.models

interface IChecklistListener {
    fun onSuccess(checklistList: List<ChecklistItem>)
    fun onFailed(error: String?)
}