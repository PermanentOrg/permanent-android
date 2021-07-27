package org.permanent.permanent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ValidatorTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun validator_CorrectName_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidName("John Doe", errorMessageToShow)).isTrue()
    }

    @Test
    fun validator_EmptyName_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidName("", errorMessageToShow)).isFalse()
    }

    @Test
    fun validator_CorrectEmail_ReturnsTrue() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidEmail("name@email.com", errorMessageToShow)).isTrue()
    }

    @Test
    fun validator_IncorrectEmail_ReturnsFalse() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidEmail("name@email", errorMessageToShow)).isFalse()
    }

    @Test
    fun validator_EmptyEmail_ReturnsFalse() {
        val errorMessageToShow = MutableLiveData<Int>()

        assertThat(Validator.isValidEmail("", errorMessageToShow)).isFalse()
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
}