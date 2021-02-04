package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import java.util.regex.Pattern

class AccountInfoViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var appContext = application.applicationContext
    private val isBusy = MutableLiveData<Boolean>()
    private val showMessage = MutableLiveData<String>()

    private var account: Account? = null
    private val name = MutableLiveData<String>()
    private val email = MutableLiveData<String>()
    private val phone = MutableLiveData<String>()
    private val address = MutableLiveData<String>()
    private val city = MutableLiveData<String>()
    private val state = MutableLiveData<String>()
    private val postalCode = MutableLiveData<String>()
    private val country = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)

    init {
        getAccountInfo()
    }

    private fun getAccountInfo() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        accountRepository.getAccount(object : IAccountRepository.IAccountListener {

            override fun onSuccess(account: Account) {
                isBusy.value = false
                this@AccountInfoViewModel.account = account
                initFields(account)
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                showMessage.value = error
            }
        })
    }

    private fun initFields(account: Account?) {
        account?.fullName?.let { name.value = it }
        account?.primaryEmail?.let { email.value = it }
        account?.phone?.let { phone.value = it }
        account?.address?.let { address.value = it }
        account?.city?.let { city.value = it }
        account?.state?.let { state.value = it }
        account?.zipCode?.let { postalCode.value = it }
        account?.country?.let { country.value = it }
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getShowMessage(): LiveData<String> {
        return showMessage
    }

    fun getName(): LiveData<String> {
        return name
    }

    fun getEmail(): LiveData<String> {
        return email
    }

    fun getPhone(): LiveData<String> {
        return phone
    }

    fun getAddress(): LiveData<String> {
        return address
    }

    fun getCity(): LiveData<String> {
        return city
    }

    fun getState(): LiveData<String> {
        return state
    }

    fun getPostalCode(): LiveData<String> {
        return postalCode
    }

    fun getCountry(): LiveData<String> {
        return country
    }

    fun onNameTextChanged(text: Editable) {
        name.value = text.toString()
    }

    fun onEmailTextChanged(text: Editable) {
        email.value = text.toString().trim { it <= ' ' }
    }

    fun onPhoneTextChanged(text: Editable) {
        phone.value = text.toString().trim { it <= ' ' }
    }

    fun onAddressTextChanged(text: Editable) {
        address.value = text.toString().trim { it <= ' ' }
    }

    fun onCityTextChanged(text: Editable) {
        city.value = text.toString().trim { it <= ' ' }
    }

    fun onStateTextChanged(text: Editable) {
        state.value = text.toString().trim { it <= ' ' }
    }

    fun onPostalCodeTextChanged(text: Editable) {
        postalCode.value = text.toString().trim { it <= ' ' }
    }

    fun onCountryTextChanged(text: Editable) {
        country.value = text.toString().trim { it <= ' ' }
    }


    fun onSaveInfoBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = name.value?.trim()
        val email = email.value
        val phone = phone.value
        val address = address.value
        val city = city.value
        val state = state.value
        val postalCode = postalCode.value
        val country = country.value

        if (name.isNullOrEmpty()) {
            showMessage.value = appContext.getString(R.string.invalid_name_error)
            return
        }

        if (email.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage.value = appContext.getString(R.string.invalid_email_error)
            return
        }

        if (!phone.isNullOrEmpty()) {
            if (!Pattern.matches("^[+]?[0-9]{8,13}\$", phone)) {
                showMessage.value = appContext.getString(R.string.invalid_phone_error)
                return
            }
        }

        account?.fullName = name
        account?.primaryEmail = email
        account?.phone = phone
        account?.address = address
        account?.city = city
        account?.state = state
        account?.zipCode = postalCode
        account?.country = country

        account?.let {
            isBusy.value = true
            accountRepository.update(it, object : IResponseListener {
                override fun onSuccess(message: String?) {
                    isBusy.value = false
                    showMessage.value = message
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }
}