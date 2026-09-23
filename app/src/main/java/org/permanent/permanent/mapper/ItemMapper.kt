package org.permanent.permanent.mapper

import org.permanent.permanent.Constants
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.models.Share
import org.permanent.permanent.models.Status
import org.permanent.permanent.network.models.ArchiveVO
import org.permanent.permanent.network.models.FileDTO
import org.permanent.permanent.network.models.FileVO
import org.permanent.permanent.network.models.ItemDTO
import org.permanent.permanent.network.models.LocationDTO
import org.permanent.permanent.network.models.LocnVO
import org.permanent.permanent.network.models.RecordVO
import org.permanent.permanent.network.models.ShareVO
import org.permanent.permanent.network.models.TagVO


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
 * Maps a V2 /folders/{id}/children item for authenticated V2 navigation — Private and
 * Public Files, gallery, search and Shares drill-ins. Kept separate from [toRecord]
 * so the share-preview path stays byte-identical while V2 navigation is gated behind
 * FeatureFlags.useStelaMigration.
 */
fun ItemDTO.toRecordV2(includePendingInvitesAsShares: Boolean = true): Record {
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
    rec.archiveId = resolvedArchiveId()
    rec.archiveNr = resolvedArchiveNr()
    rec.parentFolderLinkId = resolvedParentFolderLinkId()
    rec.type = if (isFolder) RecordType.FOLDER else RecordType.FILE
    rec.backendType = normalizedBackendType(isFolder)
    rec.size = size ?: -1L
    // .thumb.wNNN renditions are read from the NESTED thumbnailUrls (folders send no
    // flat thumbUrl*; records duplicate them flat), and only the flat thumbnail256
    // fills the 256 slot. The nested "256" is the Archivematica access copy — blank
    // for HEIC, but the only thumbnail a Stela V2 record copy has (no renditions,
    // backend gap) — so it serves as a HEIC-guarded LAST resort in the 200 slot.
    rec.thumbnail256 = thumbnail256.orNullIfEmpty()
    rec.thumbURL200 = resolvedThumb200()
    rec.thumbURL2000 = resolvedThumb2000()
    rec.isProcessing = when (status?.substringAfterLast('.')) {
        "copying", "moving" -> true
        // A file with no thumbnails yet is still being processed (same derivation as
        // Record(ItemVO) — V2 carries no thumbStatus).
        else -> !isFolder && rec.thumbnail256 == null && rec.thumbURL200 == null
    }
    rec.shares = buildShares(rec.folderLinkId, rec.archiveId, includePendingInvitesAsShares)
    // Caller-resolved per-item role. Absent clamps to VIEWER — V1 parity: a listed
    // Record always carries a non-null role (every V1 constructor clamps the same way).
    rec.accessRole = AccessRole.fromStelaBackendValue(accessRole)

    return rec
}

// V1-shaped view of a V2 record detail, so FileData keeps its variant ladder and PDF
// routing unchanged. V2 sends no contentType, width, height or derivedDT.
fun ItemDTO.toRecordVO(): RecordVO = RecordVO().also { vo ->
    vo.recordId = recordId?.toIntOrNull()
    vo.folderId = folderId?.toIntOrNull()
    vo.folder_linkId = folderLinkId?.toIntOrNull()
    vo.parentFolderId = parentFolderId?.toIntOrNull()
    vo.parentFolder_linkId = resolvedParentFolderLinkId()
    vo.archiveId = resolvedArchiveId()
    vo.archiveNbr = resolvedArchiveNr()
    vo.accessRole = AccessRole.fromStelaBackendValue(accessRole).backendString
    vo.displayName = displayName
    vo.description = description
    vo.displayDT = displayDate?.toV1Timestamp()
    vo.createdDT = createdAt?.toV1Timestamp()
    vo.updatedDT = updatedAt?.toV1Timestamp()
    vo.derivedCreatedDT = fileCreatedAt?.toV1Timestamp()
    vo.uploadFileName = uploadFileName
    vo.type = type
    vo.status = status
    vo.size = size
    vo.thumbnail256 = thumbnail256.orNullIfEmpty()
    vo.thumbURL200 = resolvedThumb200()
    vo.thumbURL2000 = resolvedThumb2000()
    vo.LocnVO = location?.toLocnVO()
    vo.FileVOs = files?.map { it.toFileVO() }
    vo.TagVOs = tags?.map { tag -> TagVO().apply { tagId = tag.id; name = tag.name } }
}

private fun FileDTO.toFileVO(): FileVO = FileVO().also { vo ->
    vo.size = size?.takeIf { it <= Int.MAX_VALUE }?.toInt()
    vo.format = format
    vo.type = type
    vo.contentType = derivedContentType()
    vo.fileURL = fileUrl.orNullIfEmpty()
    vo.downloadURL = downloadUrl.orNullIfEmpty()
}

// "type.file.<class>.<subtype>" -> MIME, the same table iOS derives.
private fun FileDTO.derivedContentType(): String? {
    val parts = type?.split('.') ?: return null
    if (parts.size < 3 || parts[0] != "type" || parts[1] != "file") return null
    val cls = parts[2]
    val subtype = parts.getOrNull(3).orEmpty()
    if (cls == "pdf") return "application/pdf"
    if (cls !in setOf("image", "video", "audio") || subtype.isEmpty()) {
        return Constants.MEDIA_TYPE_OCTET_STREAM
    }
    return "$cls/" + if (subtype == "jpg") "jpeg" else subtype
}

private fun LocationDTO.toLocnVO(): LocnVO = LocnVO().also { vo ->
    vo.locnId = id?.toIntOrNull()
    vo.streetNumber = streetNumber
    vo.streetName = streetName
    vo.locality = locality
    vo.adminOneName = state
    vo.countryCode = countryCode
    vo.latitude = latitude
    vo.longitude = longitude
}

// V2 timestamps ("2022-01-01T00:00:00.000Z", "…+00:00") -> V1's "yyyy-MM-dd HH:mm:ss".
private fun String.toV1Timestamp(): String =
    substringBefore('.').substringBefore('+').removeSuffix("Z").replace('T', ' ')

// Records send archive/parent ids flat, folders nest them.
private fun ItemDTO.resolvedArchiveId(): Int? = (archive?.id ?: archiveId)?.toIntOrNull()

private fun ItemDTO.resolvedArchiveNr(): String? = archiveNumber ?: archive?.archiveNumber

private fun ItemDTO.resolvedParentFolderLinkId(): Int? =
    (parentFolderLinkId ?: parentFolder?.folderLinkId)?.toIntOrNull()

private fun ItemDTO.resolvedThumb200(): String? =
    thumbnailUrls?.url200.orNullIfEmpty() ?: accessCopyThumb256()

private fun ItemDTO.resolvedThumb2000(): String? = thumbnailUrls?.url2000.orNullIfEmpty()

private fun ItemDTO.accessCopyThumb256(): String? =
    thumbnailUrls?.url256.orNullIfEmpty()?.takeUnless { isHeicOriginal() }

private fun ItemDTO.isHeicOriginal(): Boolean {
    if (files.orEmpty().any {
            "original" in it.format.orEmpty() &&
                (it.type.orEmpty().contains("heic", ignoreCase = true) ||
                    it.type.orEmpty().contains("heif", ignoreCase = true))
        }
    ) return true
    val name = (uploadFileName ?: downloadName).orEmpty().lowercase()
    return name.endsWith(".heic") || name.endsWith(".heif")
}

// Folders answer with new short type forms ("private", "private-root"…) while records
// keep the legacy dotted forms ("type.record.image"). Normalize folders back to the
// dotted form so downstream consumers and the type sort see V1-shaped values;
// underscore spellings are canonicalized to hyphens here (iOS tolerates both too).
private fun ItemDTO.normalizedBackendType(isFolder: Boolean): String? = when {
    type == null -> null
    isFolder && !type.startsWith("type.") -> "type.folder.${type.replace('_', '-')}"
    else -> type
}

// Shares feed the pending-invitation badge on the list rows: it counts entries with
// PENDING status. V2 splits them into shares[] (archive shares) and pendingShares[]
// (email invitations, always pending). Item permissions stay archive-derived on the
// Private Files path — these are presentation only.
// The share sheet maps shares[] only: it lists invites from pendingShares[] itself,
// and an invite has no archive or shareId to approve.
private fun ItemDTO.buildShares(
    itemFolderLinkId: Int?,
    itemArchiveId: Int?,
    includePendingInvitesAsShares: Boolean
): MutableList<Share>? {
    val invites = pendingShares.takeIf { includePendingInvitesAsShares }
    if (shares.isNullOrEmpty() && invites.isNullOrEmpty()) return null
    val result = mutableListOf<Share>()
    shares?.forEach { share ->
        val shareArchiveId = share.archive?.id?.toIntOrNull()
        // V1 omits shares to the item's own archive; V2 returns them (live 2026-09-18).
        if (shareArchiveId != null && shareArchiveId == itemArchiveId) return@forEach
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
            thumbURL200 = share.archive?.thumbUrl200
        }
        result.add(Share(shareVO))
    }
    invites?.forEach { pendingShare ->
        val shareVO = ShareVO(itemFolderLinkId ?: 0, 0)
        shareVO.shareId = pendingShare.id?.toIntOrNull()
        shareVO.accessRole = pendingShare.accessRole
        shareVO.status = Status.PENDING.toBackendString()
        result.add(Share(shareVO))
    }
    return result.ifEmpty { null }
}

private fun String?.orNullIfEmpty(): String? = this?.takeUnless { it.isEmpty() }
