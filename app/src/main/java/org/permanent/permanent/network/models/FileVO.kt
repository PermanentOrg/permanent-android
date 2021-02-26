package org.permanent.permanent.network.models

class FileVO {
    var size: Int? = null
    var contentType: String? = null // Can be: image/jpeg, video/mp4, application/pdf etc.
    var width: Int? = null
    var height: Int? = null
    var fileURL: String? = null
    var downloadURL: String? = null
}