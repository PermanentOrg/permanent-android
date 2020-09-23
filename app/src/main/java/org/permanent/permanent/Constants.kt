package org.permanent.permanent

class Constants {
    companion object {
        val BUILD_ENV = BuildEnvOption.PROD
        const val IS_ONBOARDING_COMPLETED = "onboarding_completed"
        const val IS_WELCOME_SEEN = "is_welcome_seen"
        const val POSITION_SHARED_BY_ME_FRAGMENT = 0
        const val POSITION_SHARED_WITH_ME_FRAGMENT = 1
        const val MIN_PASSWORD_LENGTH = 8
        const val VERIFICATION_CODE_LENGTH = 4
        const val FILE_NAME = "file_name"
        const val FOLDER_NAME = "folder_name"
        const val URL_STAGING = "https://staging.permanent.org/api/"
        const val URL_PROD = "https://www.permanent.org/api/"
        const val ERROR_MFA_TOKEN = "warning.auth.mfaToken"
        const val ERROR_UNKNOWN_SIGNIN = "warning.signin.unknown"
        const val ERROR_SERVER_ERROR = "error.api.invalid_request"
        const val ERROR_NO_API_KEY = "error.api.no_key"
        const val AUTH_TYPE_MFA_VALIDATION = "type.auth.mfaValidation"
        const val PREFERENCES_NAME = "permanent_preferences"
        const val PREFERENCES_SAVED_EMAIL = "preferences_saved_email"
        const val PREFERENCES_SAVED_CSRF = "preferences_saved_csrf"
    }
}