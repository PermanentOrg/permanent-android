package org.permanent.permanent.network.models

// Per-file metadata on a V2 children record item. Only what the HEIC guard in
// ItemMapper needs: the original file is the entry whose format contains
// "original"; its type tells the source format (e.g. "type.file.image.heic").
data class FileDTO(
    val format: String?,
    val type: String?,
)
