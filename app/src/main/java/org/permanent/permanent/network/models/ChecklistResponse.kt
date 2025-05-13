package org.permanent.permanent.network.models

data class ChecklistResponse(
    val checklistItems: List<ChecklistItem>
)

data class ChecklistItem(
    val id: String,
    val title: String,
    val completed: Boolean
)
