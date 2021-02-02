package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AccountSettingsViewModel(application: Application) :
    ObservableAndroidViewModel(application) {
    private val isBusy = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()

    private val accountName = MutableLiveData<String>()
    private val email = MutableLiveData<String>()
    private val phone = MutableLiveData<String>()
    private val address = MutableLiveData<String>()
    private val city = MutableLiveData<String>()
    private val state = MutableLiveData<String>()
    private val postalCode = MutableLiveData<String>()
    private val country = MutableLiveData<String>()

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun getAccountName(): LiveData<String> {
        return accountName
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

    fun onAccountNameTextChanged(text: Editable) {
        accountName.value = text.toString().trim { it <= ' ' }
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


    fun updateAccount() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val accountName = accountName.value
        val email = email.value
        val phone = phone.value
        val address = address.value
        val city = city.value
        val state = state.value
        val postalCode = postalCode.value
        val country = country.value

        if (TextUtils.isEmpty(accountName)) {
            errorMessage.value = "Please enter your account name"
            return
        }

        if (TextUtils.isEmpty(email)) {
            errorMessage.value = "Please enter your email"
            return
        }

        if (TextUtils.isEmpty(phone)) {
            errorMessage.value = "Please retype your phone"
            return
        }

        if (TextUtils.isEmpty(address)) {
            errorMessage.value = "Please enter your address"
            return
        }

        if (TextUtils.isEmpty(city)) {
            errorMessage.value = "Please enter your city"
            return
        }

        if (TextUtils.isEmpty(state)) {
            errorMessage.value = "Please retype your state or region"
            return
        }

        if (TextUtils.isEmpty(postalCode)) {
            errorMessage.value = "Please enter your postal code"
            return
        }

        if (TextUtils.isEmpty(country)) {
            errorMessage.value = "Please enter your country"
            return
        }
        //TODO update account
    }
}