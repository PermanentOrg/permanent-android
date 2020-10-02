package org.permanent.permanent.network

import org.permanent.permanent.BuildEnvOption
import org.permanent.permanent.Constants
import org.permanent.permanent.network.models.*

class RequestContainer(var csrf: String?) {
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
        RequestVO.data?.get(0)?.AccountVO = account
        return this
    }

    fun addAccount(accountId: String, email: String, phoneNumber: String): RequestContainer {
        val account = AccountVO()
        account.accountId = accountId
        account.primaryEmail = email
        account.primaryPhone = phoneNumber
        RequestVO.data?.get(0)?.AccountVO = account
        return this
    }

    fun addAccount(fullName: String, email: String): RequestContainer {
        val accountVO = AccountVO()
        accountVO.fullName = fullName
        accountVO.primaryEmail = email
        accountVO.optIn = false
        accountVO.agreed = true
        RequestVO.data?.get(0)?.AccountVO = accountVO
        return this
    }

    fun addAccountPassword(password: String): RequestContainer {
        val accountPassword = AccountPasswordVO()
        accountPassword.password = password
        RequestVO.data?.get(0)?.AccountPasswordVO = accountPassword
        return this
    }

    fun addAccountPassword(password: String, passwordVerify: String): RequestContainer {
        addAccountPassword(password)
        RequestVO.data?.get(0)?.AccountPasswordVO?.passwordVerify = passwordVerify
        return this
    }

    fun addAuth(authToken: String): RequestContainer {
        val auth = AuthVO()
        auth.token = authToken
        auth.type = Constants.AUTH_TYPE_MFA_VALIDATION
        RequestVO.data?.get(0)?.AuthVO = auth
        return this
    }
}