package org.permanent.permanent

class Constants {
    companion object {
        const val IS_ONBOARDING_COMPLETED = "onboarding_completed"
        const val IS_WELCOME_SEEN = "is_welcome_seen"
        const val POSITION_SHARED_BY_ME_FRAGMENT = 0
        const val POSITION_SHARED_WITH_ME_FRAGMENT = 1
        const val MIN_PASSWORD_LENGTH = 8
        const val FILE_NAME = "file_name"
        const val FOLDER_NAME = "folder_name"
        val BUILD_ENV = BuildEnvOption.PROD
        const val URL_STAGING = "https://staging.permanent.org/api/"
        const val URL_PROD = "https://www.permanent.org/api/"
    }
}