package org.permanent.permanent.network.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.models.ShareByUrl

class Shareby_urlVO() : Parcelable {
    var shareUrl: String? = null
    var shareby_urlId: Int? = null
    var autoApproveToggle: Int? = null // boolean (0 or 1)
    var previewToggle: Int? = null // boolean (0 or 1)
    var expiresDT: String? = null // can be null for no expiration
    var maxUses: Int? = null // can be 0 for unlimited uses
    var byAccountId: Int? = null
    var byArchiveId: Int? = null
    var urlToken: String? = null
    var FolderVO: FolderVO? = null
    var RecordVO: RecordVO? = null
    var ArchiveVO: ArchiveVO? = null
    var AccountVO: AccountVO? = null
    var ShareVO: ShareVO? = null

    constructor(parcel: Parcel) : this() {
        shareUrl = parcel.readString()
        shareby_urlId = parcel.readValue(Int::class.java.classLoader) as? Int
        autoApproveToggle = parcel.readValue(Int::class.java.classLoader) as? Int
        previewToggle = parcel.readValue(Int::class.java.classLoader) as? Int
        expiresDT = parcel.readString()
        maxUses = parcel.readValue(Int::class.java.classLoader) as? Int
        byAccountId = parcel.readValue(Int::class.java.classLoader) as? Int
        byArchiveId = parcel.readValue(Int::class.java.classLoader) as? Int
        urlToken = parcel.readString()
    }

    constructor(shareByUrl: ShareByUrl) : this() {
        shareUrl = shareByUrl.shareUrl
        shareby_urlId = shareByUrl.shareByUrlId
        autoApproveToggle = shareByUrl.autoApproveToggle
        previewToggle = shareByUrl.previewToggle
        expiresDT = shareByUrl.expiresDT
        maxUses = shareByUrl.maxUses
        byAccountId = shareByUrl.byAccountId
        byArchiveId = shareByUrl.byArchiveId
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(shareUrl)
        parcel.writeValue(shareby_urlId)
        parcel.writeValue(autoApproveToggle)
        parcel.writeValue(previewToggle)
        parcel.writeString(expiresDT)
        parcel.writeValue(maxUses)
        parcel.writeValue(byAccountId)
        parcel.writeValue(byArchiveId)
        parcel.writeString(urlToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Shareby_urlVO> {
        override fun createFromParcel(parcel: Parcel): Shareby_urlVO {
            return Shareby_urlVO(parcel)
        }

        override fun newArray(size: Int): Array<Shareby_urlVO?> {
            return arrayOfNulls(size)
        }
    }
}