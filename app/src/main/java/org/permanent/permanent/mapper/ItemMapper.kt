package org.permanent.permanent.mapper

import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.network.models.ItemDTO


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