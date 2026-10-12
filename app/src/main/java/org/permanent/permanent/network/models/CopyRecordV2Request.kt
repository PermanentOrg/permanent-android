package org.permanent.permanent.network.models

// The server requires the destination folderId as a numeric string.
data class CopyRecordV2Request(val destinationFolderId: String)
