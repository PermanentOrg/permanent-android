package org.permanent.permanent.network.models

class DownloadData(datum: Datum?, fileVO: FileVO?) {
    var displayName: String? = datum?.RecordVO?.displayName
    var downloadURL: String? = fileVO?.downloadURL
    var contentType: String? = fileVO?.contentType
    var fileName: String? = datum?.RecordVO?.uploadFileName
}