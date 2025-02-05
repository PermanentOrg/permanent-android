package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Validator
import org.permanent.permanent.models.Account
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.EventAction
import org.permanent.permanent.network.IResponseListener
import org.permanent.permanent.repositories.AccountRepositoryImpl
import org.permanent.permanent.repositories.EventsRepositoryImpl
import org.permanent.permanent.repositories.IAccountRepository
import org.permanent.permanent.repositories.IEventsRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class AccountViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private var appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val isBusy = MutableLiveData<Boolean>()
    private val showMessage = MutableLiveData<String?>()
    private val showDeleteAccountDialog = SingleLiveEvent<Void?>()
    private var account: Account? = null
    private val name = MutableLiveData<String>()
    private val email = MutableLiveData<String>()
    private val phone = MutableLiveData<String>()
    private val address = MutableLiveData<String>()
    private val addressTwo = MutableLiveData<String>()
    private val city = MutableLiveData<String>()
    private val state = MutableLiveData<String>()
    private val postalCode = MutableLiveData<String>()
    private val country = MutableLiveData<String>()
    private var accountRepository: IAccountRepository = AccountRepositoryImpl(application)
    private var eventsRepository: IEventsRepository = EventsRepositoryImpl(application)

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
        account?.addressTwo?.let { addressTwo.value = it }
        account?.city?.let { city.value = it }
        account?.state?.let { state.value = it }
        account?.zipCode?.let { postalCode.value = it }
        account?.country?.let { country.value = it }
    }

    fun sendEvent(action: EventAction, data: Map<String, String> = mapOf()) {
        eventsRepository.sendEventAction(
            eventAction = action,
            accountId = prefsHelper.getAccountId(),
            data = data
        )
    }

    fun getOnShowDeleteAccountDialog(): SingleLiveEvent<Void?> = showDeleteAccountDialog

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String?> = showMessage

    fun getName(): LiveData<String> = name

    fun getEmail(): LiveData<String> = email

    fun getPhone(): LiveData<String> = phone

    fun getAddress(): LiveData<String> = address

    fun getAddressTwo(): LiveData<String> = addressTwo

    fun getCity(): LiveData<String> = city

    fun getState(): LiveData<String> = state

    fun getPostalCode(): LiveData<String> = postalCode

    fun getCountry(): LiveData<String> = country

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

    fun onAddressLineTwoTextChanged(text: Editable) {
        addressTwo.value = text.toString()
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
        val addressTwo = addressTwo.value?.trim()
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
        account?.addressTwo = addressTwo
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
                    sendEvent(AccountEventAction.UPDATE)
                }

                override fun onFailed(error: String?) {
                    isBusy.value = false
                    showMessage.value = error
                }
            })
        }
    }
}