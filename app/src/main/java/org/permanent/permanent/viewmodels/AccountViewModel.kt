package org.permanent.permanent.viewmodels

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Validator
import org.permanent.permanent.models.Account
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository

class AccountViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var appContext = application.applicationContext
    private val isBusy = MutableLiveData<Boolean>()
    private val showMessage = MutableLiveData<String>()
    private val showDeleteAccountDialog = SingleLiveEvent<Void>()
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
                this@AccountViewModel.account = account
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

    fun getOnShowDeleteAccountDialog(): SingleLiveEvent<Void> {
        return showDeleteAccountDialog
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
        address.value = text.toString()
    }

    fun onCityTextChanged(text: Editable) {
        city.value = text.toString()
    }

    fun onStateTextChanged(text: Editable) {
        state.value = text.toString()
    }

    fun onPostalCodeTextChanged(text: Editable) {
        postalCode.value = text.toString()
    }

    fun onCountryTextChanged(text: Editable) {
        country.value = text.toString()
    }

    fun onDeleteAccountBtnClick() {
        showDeleteAccountDialog.call()
    }

    fun onSaveInfoBtnClick() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val name = name.value?.trim()
        val email = email.value?.trim()
        val phone = phone.value
        val address = address.value?.trim()
        val city = city.value?.trim()
        val state = state.value?.trim()
        val postalCode = postalCode.value
        val country = country.value?.trim()

        if (!Validator.isValidName(appContext, name, null, showMessage)) return

        if (!Validator.isValidEmail(appContext, email, null, showMessage)) return

        if (!Validator.isValidPhone(appContext, phone, showMessage)) return

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