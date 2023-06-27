package org.permanent.permanent.repositories

import android.content.Context
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.network.NetworkClient
import org.permanent.permanent.network.models.AccountVO
import org.permanent.permanent.network.models.ResponseVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountRepositoryImpl(context: Context) : IAccountRepository {

    private val appContext = context.applicationContext
    private val prefsHelper =
        PreferencesHelper(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    override fun signUp(
        fullName: String,
        email: String,
        password: String,
        listener: IAccountRepository.IAccountListener
    ) {
        NetworkClient.instance().signUp(fullName, email, password)
            .enqueue(object : Callback<AccountVO> {

                override fun onResponse(call: Call<AccountVO>, response: Response<AccountVO>) {
                    val accountVO = response.body()
                    if (accountVO != null) {
                        listener.onSuccess(Account(accountVO))
                    } else {
                        listener.onFailed(
                            if (response.errorBody()?.string()
                                    ?.contains(Constants.ERROR_EMAIL_DUPLICATED) == true
                            ) appContext.getString(R.string.sign_up_email_in_use_error)
                            else appContext.getString(R.string.account_create_failed)
                        )
                    }
                }

                override fun onFailure(call: Call<AccountVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun getAccount(listener: IAccountRepository.IAccountListener) {
        val accountId = prefsHelper.getAccountId()

        if (accountId != 0) {
            NetworkClient.instance().getAccount(accountId)
                .enqueue(object : Callback<ResponseVO> {

                    override fun onResponse(
                        call: Call<ResponseVO>,
                        response: Response<ResponseVO>
                    ) {
                        val responseVO = response.body()
                        if (response.isSuccessful && responseVO?.isSuccessful!!) {
                            listener.onSuccess(Account(responseVO.getAccountVO()))
                        } else {
                            listener.onFailed(
                                responseVO?.getMessages()?.get(0)
                                    ?: response.errorBody()?.toString()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                        listener.onFailed(t.message)
                    }
                })
        }
    }

    override fun getSessionAccount(listener: IAccountRepository.IAccountListener) {
        NetworkClient.instance().getSessionAccount()
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(
                    call: Call<ResponseVO>,
                    response: Response<ResponseVO>
                ) {
                    val responseVO = response.body()
                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess(Account(responseVO.getAccountVO()))
                    } else {
                        listener.onFailed(
                            responseVO?.getMessages()?.get(0)
                                ?: response.errorBody()?.toString()
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun update(account: Account, listener: IResponseListener) {
        NetworkClient.instance().updateAccount(account)
            .enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess(appContext.getString(R.string.account_update_success))
                    } else {
                        val errorMessage: String? =
                            when (val responseMessage = responseVO?.getMessages()?.get(0)) {
                                Constants.ERROR_PHONE_INVALID -> appContext.getString(R.string.invalid_phone_error)
                                else -> responseMessage
                            }
                        listener.onFailed(errorMessage)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
    }

    override fun changeDefaultArchive(defaultArchiveId: Int, listener: IResponseListener) {
        prefsHelper.getAccountEmail()?.let {
            NetworkClient.instance().changeDefaultArchive(
                prefsHelper.getAccountId(),
                it,
                defaultArchiveId
            ).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(
                    call: Call<ResponseVO>,
                    response: Response<ResponseVO>
                ) {
                    val responseVO = response.body()
                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess(appContext.getString(R.string.archive_update_default_success))
                    } else {
                        listener.onFailed(responseVO?.getMessages()?.get(0))
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }

    override fun delete(listener: IResponseListener) {
        val accountId = prefsHelper.getAccountId()
        if (accountId != 0) {
            NetworkClient.instance().deleteAccount(accountId)
                .enqueue(object : Callback<ResponseVO> {

                    override fun onResponse(
                        call: Call<ResponseVO>,
                        response: Response<ResponseVO>
                    ) {
                        val responseVO = response.body()
                        if (response.isSuccessful && responseVO?.isSuccessful!!) {
                            listener.onSuccess(appContext.getString(R.string.account_delete_success))
                        } else {
                            listener.onFailed(
                                responseVO?.getMessages()?.get(0)
                                    ?: response.errorBody()?.toString()
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                        listener.onFailed(t.message)
                    }
                })
        }
    }

    override fun changePassword(
        currentPassword: String, newPassword: String, retypedPassword: String,
        listener: IResponseListener
    ) {
        val accountId = prefsHelper.getAccountId()

        if (accountId != 0) {
            NetworkClient.instance().changePassword(
                accountId, currentPassword, newPassword, retypedPassword
            ).enqueue(object : Callback<ResponseVO> {

                override fun onResponse(call: Call<ResponseVO>, response: Response<ResponseVO>) {
                    val responseVO = response.body()
                    if (response.isSuccessful && responseVO?.isSuccessful!!) {
                        listener.onSuccess(appContext.getString(R.string.security_password_update_success))
                    } else {
                        val errorMessage: String? =
                            when (val responseMessage = responseVO?.getMessages()?.get(0)) {
                                Constants.ERROR_PASSWORD_COMPLEXITY_LOW -> appContext.getString(R.string.security_error_low_password_complexity)
                                Constants.ERROR_PASSWORD_NO_MATCH -> appContext.getString(R.string.security_error_password_no_match)
                                Constants.ERROR_PASSWORD_OLD_INCORRECT -> appContext.getString(R.string.security_error_incorrect_old_password)
                                else -> responseMessage
                            }
                        listener.onFailed(errorMessage)
                    }
                }

                override fun onFailure(call: Call<ResponseVO>, t: Throwable) {
                    listener.onFailed(t.message)
                }
            })
        }
    }
}