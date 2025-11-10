package org.permanent.permanent.viewmodels

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.R
import org.permanent.permanent.ui.bytesToHumanReadableString
import org.permanent.permanent.ui.recordMenu.RecordUiModel
import org.permanent.permanent.ui.toDisplayDate

data class HeaderInfo(
    val thumbUrl: String?,
    val name: String,
    val infoText: String,
    val showMultipleShadow: Boolean
)

class SelectionMenuViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val _menuItems = MutableStateFlow<List<RecordMenuItem>>(emptyList())
    val menuItems: StateFlow<List<RecordMenuItem>> = _menuItems

    private val _headerInfo = MutableStateFlow<HeaderInfo?>(null)
    val headerInfo: StateFlow<HeaderInfo?> = _headerInfo

    fun initWithSelection(records: List<RecordUiModel>) {
        if (records.isEmpty()) return

        val first = records.first()
        val files = records.filterNot { it.isFolder }
        val folders = records.filter { it.isFolder }

        val titleText = if (records.size == 1) first.name else appContext.getString(
            R.string.items_selected,
            records.size
        )

        val totalFileSize = files.sumOf { it.sizeBytes.coerceAtLeast(0L) }
        val totalSizeText = if (totalFileSize > 0) bytesToHumanReadableString(totalFileSize) else ""

        val firstFolderDate = folders.firstOrNull()?.createdDate?.toDisplayDate() ?: ""

        val infoText = when {
            // Single selection: 1 file → size + date
            records.size == 1 && !first.isFolder -> {
                val date = first.createdDate.toDisplayDate()
                listOfNotNull(totalSizeText.ifEmpty { null }, date.ifEmpty { null })
                    .joinToString(" • ")
            }

            // Single selection: 1 folder → only date
            records.size == 1 && first.isFolder -> {
                first.createdDate.toDisplayDate()
            }

            // Multiple: only files
            files.isNotEmpty() && folders.isEmpty() -> totalSizeText

            // Files + folders
            files.isNotEmpty() && folders.isNotEmpty() -> {
                if (totalSizeText.isNotEmpty() && firstFolderDate.isNotEmpty())
                    "$totalSizeText • $firstFolderDate"
                else totalSizeText.ifEmpty { firstFolderDate }
            }

            // Only folders
            files.isEmpty() && folders.isNotEmpty() -> firstFolderDate

            else -> ""
        }

        _headerInfo.value = HeaderInfo(
            thumbUrl = first.thumbUrl,
            name = titleText,
            infoText = infoText,
            showMultipleShadow = records.size > 1
        )

        _menuItems.value = buildMenuItems(folders.isEmpty())
    }

    private fun buildMenuItems(includeEditMetadata: Boolean = true): List<RecordMenuItem> {
        val items = mutableListOf<RecordMenuItem>()

        if (includeEditMetadata) {
            items += RecordMenuItem.EditMetadata
        }

        items += listOf(
            RecordMenuItem.Copy,
            RecordMenuItem.Move,
            RecordMenuItem.Delete
        )

        return items
    }
}