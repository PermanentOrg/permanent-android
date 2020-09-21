package org.permanent.permanent.network.models

import org.permanent.permanent.BuildEnvOption
import org.permanent.permanent.Constants

class RequestContainer(var csrf: String) {
    // Don't rename this property, is used in the api call
    private var RequestVO: RequestVO = RequestVO()

    init {
        //TODO STORE IN PREFERENCES
        val PERM_API_KEY_MOBILE_STAGING = "0f6c8cf215a2a73a174ff45807a76be3"
        val PERM_API_KEY_MOBILE_PROD = "5aef7dd1f32e0d9ca57290e3c82b59db"

        if (Constants.BUILD_ENV === BuildEnvOption.STAGING) {
            RequestVO.apiKey = PERM_API_KEY_MOBILE_STAGING
        } else {
            RequestVO.apiKey = PERM_API_KEY_MOBILE_PROD
        }
        RequestVO.csrf = csrf
        val dataList = (RequestVO.data as ArrayList)
        dataList.add(Datum())
        RequestVO.data = dataList
    }

    fun addAccount(email: String): RequestContainer {
        val account = AccountVO()
        account.primaryEmail = email
        RequestVO.data[0].AccountVO = account
        return this
    }

    fun addAccountPassword(password: String): RequestContainer {
        val accountPassword = AccountPasswordVO()
        accountPassword.password = password
        RequestVO.data[0].AccountPasswordVO = accountPassword
        return this
    }
}