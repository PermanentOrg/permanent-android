package org.permanent.permanent.network

import org.permanent.permanent.network.models.TwoFAVO

interface ITwoFAListener {
    fun onSuccess(twoFAVOList: List<TwoFAVO>?)
    fun onFailed(error: String?)
}