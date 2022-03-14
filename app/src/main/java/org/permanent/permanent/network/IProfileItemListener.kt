package org.permanent.permanent.network

import org.permanent.permanent.models.ProfileItem

interface IProfileItemListener {
    fun onSuccess(profileItem: ProfileItem)
    fun onFailed(error: String?)
}