package org.permanent.permanent.models

import android.os.Parcel
import android.os.Parcelable
import org.permanent.permanent.network.models.Shareby_urlVO

class ShareByUrl() : Parcelable {
    var shareUrl: String? = null
    var shareByUrlId: Int? = null
    var autoApproveToggle: Int? = null // boolean (0 or 1)
    var previewToggle: Int? = null // boolean (0 or 1)
    var defaultAccessRole: String? = null
    var expiresDT: String? = null // can be null for no expiration
    var maxUses: Int? = null // can be 0 for unlimited uses
    var byAccountId: Int? = null
    var byArchiveId: Int? = null

    constructor(shareByUrlVO: Shareby_urlVO) : this() {
        shareUrl = shareByUrlVO.shareUrl
        shareByUrlId = shareByUrlVO.shareby_urlId
        autoApproveToggle = shareByUrlVO.autoApproveToggle
        previewToggle = shareByUrlVO.previewToggle
        defaultAccessRole = shareByUrlVO.defaultAccessRole
        expiresDT = shareByUrlVO.expiresDT
        maxUses = shareByUrlVO.maxUses
        byAccountId = shareByUrlVO.byAccountId
        byArchiveId = shareByUrlVO.byArchiveId
    }

    constructor(parcel: Parcel) : this() {
        shareUrl = parcel.readString()
        shareByUrlId = parcel.readValue(Int::class.java.classLoader) as? Int
        autoApproveToggle = parcel.readValue(Int::class.java.classLoader) as? Int
        previewToggle = parcel.readValue(Int::class.java.classLoader) as? Int
        defaultAccessRole = parcel.readString()
        expiresDT = parcel.readString()
        maxUses = parcel.readValue(Int::class.java.classLoader) as? Int
        byAccountId = parcel.readValue(Int::class.java.classLoader) as? Int
        byArchiveId = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(shareUrl)
        parcel.writeValue(shareByUrlId)
        parcel.writeValue(autoApproveToggle)
        parcel.writeValue(previewToggle)
        parcel.writeString(defaultAccessRole)
        parcel.writeString(expiresDT)
        parcel.writeValue(maxUses)
        parcel.writeValue(byAccountId)
        parcel.writeValue(byArchiveId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShareByUrl> {
        override fun createFromParcel(parcel: Parcel): ShareByUrl {
            return ShareByUrl(parcel)
        }

        override fun newArray(size: Int): Array<ShareByUrl?> {
            return arrayOfNulls(size)
        }
    }
}