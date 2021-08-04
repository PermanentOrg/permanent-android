package org.permanent.permanent

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class ValidatorTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun validator_CorrectName_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidName(Mockito.mock(Context::class.java), "John Doe", errorMessageToShow, null)).isTrue()
    }

    @Test
    fun validator_EmptyName_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidName(Mockito.mock(Context::class.java), "", errorMessageToShow, null)).isFalse()
    }

    @Test
    fun validator_CorrectEmail_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidEmail(Mockito.mock(Context::class.java),"name@email.com", errorMessageToShow, null)).isTrue()
    }

    @Test
    fun validator_IncorrectEmail_ReturnsFalse() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidEmail(Mockito.mock(Context::class.java),"name@email", errorMessageToShow, null)).isFalse()
    }

    @Test
    fun validator_EmptyEmail_ReturnsFalse() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidEmail(Mockito.mock(Context::class.java),"", errorMessageToShow, null)).isFalse()
    }

    @Test
    fun validator_CorrectPassword_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidPassword("1234abcd", errorMessageToShow)).isTrue()
    }

    @Test
    fun validator_TooShortPassword_ReturnsFalse() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidPassword("1234", errorMessageToShow)).isFalse()
    }

    @Test
    fun validator_EmptyPassword_ReturnsFalse() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidPassword("", errorMessageToShow)).isFalse()
    }

    @Test
    fun validator_CorrectPhone_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<String>()

        assertThat(Validator.isValidPhone(Mockito.mock(Context::class.java),"+40773909845", errorMessageToShow)).isTrue()
    }

    @Test
    fun validator_EmptyPhone_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<String>()

        assertThat(Validator.isValidPhone(Mockito.mock(Context::class.java),"", errorMessageToShow)).isTrue()
    }

    @Test
    fun validator_IncorrectPhone_ReturnsFalse() {
        val errorMessageToShow = MutableLiveData<String>()

        assertThat(Validator.isValidPhone(Mockito.mock(Context::class.java),"012234", errorMessageToShow)).isFalse()
    }
}
