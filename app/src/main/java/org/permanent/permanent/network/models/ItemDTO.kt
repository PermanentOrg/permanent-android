package org.permanent.permanent.network.models

data class ItemDTO(
    val folderId: String?,
    val recordId: String?,
    val folderLinkId: String?,
    val displayName: String?,
    val displayDate: String?,
    val size: Long?,
    val thumbUrl200: String?,
    var thumbUrl2000: String?,
    val archive: ArchiveDTO?,
    // Fields below are consumed by the V2 navigation path (VSP-1778). On the wire,
    // records send parent/thumbnail fields FLAT while folders nest them under
    // parentFolder/thumbnailUrls; records date via displayDate, folders via
    // displayTimestamp.
    val type: String? = null,
    val status: String? = null,
    val displayTimestamp: String? = null,
    val archiveNumber: String? = null,
    val thumbnail256: String? = null,
    val parentFolderId: String? = null,
    val parentFolderLinkId: String? = null,
    val parentFolder: ParentFolderDTO? = null,
    val thumbnailUrls: ThumbnailUrlsDTO? = null,
    val shares: List<ItemShareDTO>? = null,
    val pendingShares: List<PendingShareDTO>? = null,
    // HEIC detection for the access-copy thumbnail fallback in ItemMapper; the
    // file names are the fallback signal when files[] is absent.
    val uploadFileName: String? = null,
    val downloadName: String? = null,
    val files: List<FileDTO>? = null,
)
