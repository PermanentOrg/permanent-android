package org.permanent.permanent.network.models

import com.squareup.moshi.Json

// Nested thumbnail renditions keyed by width. "256" is the Archivematica access
// copy — blank for HEIC — and must never be used as a thumbnail source; only the
// item's flat thumbnail256 counts (see ItemMapper.toRecordV2).
data class ThumbnailUrlsDTO(
    @field:Json(name = "200") val url200: String?,
    @field:Json(name = "256") val url256: String?,
    @field:Json(name = "500") val url500: String?,
    @field:Json(name = "1000") val url1000: String?,
    @field:Json(name = "2000") val url2000: String?,
)
