package org.permanent.permanent.ui.myFiles.checklist

import org.permanent.permanent.R
import org.permanent.permanent.network.models.ChecklistItem

enum class ChecklistItemType(val id: String, val iconResId: Int) {
    ARCHIVE_CREATED("archiveCreated", R.drawable.ic_archives_blue),
    STORAGE_REDEEMED("storageRedeemed", R.drawable.ic_gift_blue_light),
    FIRST_UPLOAD("firstUpload", R.drawable.ic_file_upload_blue_light),
    ARCHIVE_STEWARD("archiveSteward", R.drawable.ic_archive_steward_blue),
    LEGACY_CONTACT("legacyContact", R.drawable.ic_legacy_contact_blue),
    ARCHIVE_PROFILE("archiveProfile", R.drawable.ic_archive_profile_blue),
    PUBLISH_CONTENT("publishContent", R.drawable.ic_public_blue);

    companion object {
        fun fromId(id: String): ChecklistItemType? = values().find { it.id == id }
    }
}

fun ChecklistItem.toChecklistType(): ChecklistItemType? =
    ChecklistItemType.fromId(this.id)