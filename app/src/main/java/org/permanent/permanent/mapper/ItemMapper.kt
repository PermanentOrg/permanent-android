package org.permanent.permanent.mapper

import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.models.ArchiveVO
import org.permanent.permanent.network.models.ItemDTO
import org.permanent.permanent.network.models.ShareVO


fun ItemDTO.toRecord(): Record {
    val rec = Record(
        recordId = recordId?.toIntOrNull() ?: 0,
        folderLinkId = folderLinkId?.toIntOrNull() ?: 0
    )

    rec.id = folderId?.toIntOrNull() ?: recordId?.toIntOrNull()
    rec.folderId = folderId?.toIntOrNull()
    rec.recordId = recordId?.toIntOrNull()
    rec.displayName = displayName
    rec.displayDate = displayDate?.replace("T", " ")
    rec.thumbURL200 = thumbUrl200
    rec.thumbURL2000 = thumbUrl2000
    rec.archiveId = archive?.id?.toIntOrNull()
    rec.archiveNr = archive?.name
    rec.type = if (folderId != null) RecordType.FOLDER else RecordType.FILE
    rec.size = size ?: -1L

    return rec
}

/**
 * Maps a V2 /folders/{id}/children item for authenticated Private Files browsing
 * (VSP-1778). Kept separate from [toRecord] so the share-preview path stays
 * byte-identical while V2 navigation is gated behind FeatureFlags.useStelaMigration.
 */
fun ItemDTO.toRecordV2(): Record {
    val isFolder = folderId != null
    val rec = Record(
        recordId = recordId?.toIntOrNull() ?: -1,
        folderLinkId = folderLinkId?.toIntOrNull() ?: -1
    )

    rec.id = folderId?.toIntOrNull() ?: recordId?.toIntOrNull()
    rec.folderId = folderId?.toIntOrNull()
    rec.recordId = recordId?.toIntOrNull()
    rec.displayName = displayName
    // Records date via displayDate, folders via displayTimestamp.
    rec.displayDate = (displayDate ?: displayTimestamp)?.replace("T", " ")
    rec.archiveId = archive?.id?.toIntOrNull()
    rec.archiveNr = archiveNumber ?: archive?.archiveNumber
    rec.parentFolderLinkId = (parentFolderLinkId ?: parentFolder?.folderLinkId)?.toIntOrNull()
    rec.type = if (isFolder) RecordType.FOLDER else RecordType.FILE
    rec.backendType = normalizedBackendType(isFolder)
    rec.size = size ?: -1L
    // thumbnailUrls."256" is the Archivematica access copy — blank for HEIC — so only
    // the flat thumbnail256 counts (it appears once processing finishes); the UI then
    // falls through to the .thumb.wNNN renditions, read from the NESTED thumbnailUrls —
    // records also send flat thumbUrl* duplicates, folders don't, so nested covers both.
    // The wire may send empty strings instead of null — treat as absent.
    rec.thumbnail256 = thumbnail256.orNullIfEmpty()
    rec.thumbURL200 = thumbnailUrls?.url200.orNullIfEmpty()
    rec.thumbURL2000 = thumbnailUrls?.url2000.orNullIfEmpty()
    rec.isProcessing = when (status?.substringAfterLast('.')) {
        "copying", "moving" -> true
        // A file with no thumbnails yet is still being processed (same derivation as
        // Record(ItemVO) — V2 carries no thumbStatus).
        else -> !isFolder && rec.thumbnail256 == null && rec.thumbURL200 == null
    }
    rec.shares = buildShares(rec.folderLinkId)

    return rec
}

// Folders answer with new short type forms ("private", "root.private"…) while records
// keep the legacy dotted forms ("type.record.image"). Normalize folders back to the
// dotted form so downstream consumers and the type sort see V1-shaped values.
private fun ItemDTO.normalizedBackendType(isFolder: Boolean): String? = when {
    type == null -> null
    isFolder && !type.startsWith("type.") -> "type.folder.$type"
    else -> type
}

// Shares feed the pending-invitation badge on the list rows: it counts entries with
// PENDING status. V2 splits them into shares[] (archive shares) and pendingShares[]
// (email invitations, always pending). Item permissions stay archive-derived on the
// Private Files path — these are presentation only.
private fun ItemDTO.buildShares(itemFolderLinkId: Int?): MutableList<Share>? {
    if (shares.isNullOrEmpty() && pendingShares.isNullOrEmpty()) return null
    val result = mutableListOf<Share>()
    shares?.forEach { share ->
        val shareArchiveId = share.archive?.id?.toIntOrNull()
        val shareVO = ShareVO(itemFolderLinkId ?: 0, shareArchiveId ?: 0)
        shareVO.shareId = share.id?.toIntOrNull()
        shareVO.accessRole = share.accessRole
        // Same short-vs-dotted split as type: tolerate "pending" and "status.generic.pending".
        shareVO.status = if (share.status?.substringAfterLast('.') == "pending") {
            Status.PENDING.toBackendString()
        } else share.status
        shareVO.ArchiveVO = ArchiveVO().apply {
            archiveId = shareArchiveId
            archiveNbr = share.archive?.archiveNumber
            fullName = share.archive?.name
        }
        result.add(Share(shareVO))
    }
    pendingShares?.forEach { pendingShare ->
        val shareVO = ShareVO(itemFolderLinkId ?: 0, 0)
        shareVO.shareId = pendingShare.id?.toIntOrNull()
        shareVO.accessRole = pendingShare.accessRole
        shareVO.status = Status.PENDING.toBackendString()
        result.add(Share(shareVO))
    }
    return result.ifEmpty { null }
}

private fun String?.orNullIfEmpty(): String? = this?.takeUnless { it.isEmpty() }
