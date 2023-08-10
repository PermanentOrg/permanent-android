package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable

enum class ThumbStatus(private val backendString: String?) : Parcelable {
    NULL(null),
    OK("status.generic.ok"),
    RECORD_NEEDS_THUMB("status.record.needs_thumb"), // This record has just been uploaded and is processing
    FOLDER_COPYING("status.folder.copying"), // This folder or some of it's contents are being copied and cannot be access until the copy is completed.
    FOLDER_MOVING("status.folder.moving"), // This folder or some of it's contents are being moved and cannot be accessed until the move is completed.
    FOLDER_NEW("status.folder.new"), // The folder is new and needs to be processed
    FOLDER_FOLDER_GENTHUMB("status.folder.genthumb"), // The folder thumbnail is regenerating
    FOLDER_FOLDER_NESTED("status.folder.nested"), // The folder contains only folders, so has no thumbnail
    FOLDER_EMPTY("status.folder.empty"), // The folder is empty, so has no thumbnail
    FOLDER_BROKEN_THUMBNAIL("status.folder.broken_thumbnail"), // The folder thumbnail failed to generate
    FOLDER_NO_THUMBNAIL_CANDIDATES("status.folder.no_thumbnail_candidates"); // The folder has records, but not the kind we make into thumbnails

    fun toBackendString(): String? = backendString

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Status> {
        override fun createFromParcel(parcel: Parcel): Status {
            return Status.values()[parcel.readInt()]
        }

        fun createFromBackendString(backendString: String?): ThumbStatus {
            return when (backendString) {
                NULL.backendString -> NULL
                RECORD_NEEDS_THUMB.backendString -> RECORD_NEEDS_THUMB
                FOLDER_COPYING.backendString -> FOLDER_COPYING
                FOLDER_MOVING.backendString -> FOLDER_MOVING
                FOLDER_NEW.backendString -> FOLDER_NEW
                FOLDER_FOLDER_GENTHUMB.backendString -> FOLDER_FOLDER_GENTHUMB
                FOLDER_FOLDER_NESTED.backendString -> FOLDER_FOLDER_NESTED
                FOLDER_EMPTY.backendString -> FOLDER_EMPTY
                FOLDER_BROKEN_THUMBNAIL.backendString -> FOLDER_BROKEN_THUMBNAIL
                FOLDER_NO_THUMBNAIL_CANDIDATES.backendString -> FOLDER_NO_THUMBNAIL_CANDIDATES
                else -> OK
            }
        }

        override fun newArray(size: Int): Array<Status?> {
            return arrayOfNulls(size)
        }
    }
}