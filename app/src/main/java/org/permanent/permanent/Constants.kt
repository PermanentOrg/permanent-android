package org.permanent.permanent

class Constants {
    companion object {
        val BUILD_ENV = BuildEnvOption.STAGING
        const val POSITION_SHARED_BY_ME_FRAGMENT = 0
        const val POSITION_SHARED_WITH_ME_FRAGMENT = 1
        const val MIN_PASSWORD_LENGTH = 8
        const val VERIFICATION_CODE_LENGTH = 4
        const val REQUEST_CODE_FILE_SELECT = 200
        const val REQUEST_CODE_IMAGE_CAPTURE = 201
        const val REQUEST_CODE_VIDEO_CAPTURE = 203
        const val FOLDER_NAME = "folder_name"
        const val MY_FILES_FOLDER = "My Files"
        const val URL_STAGING = "https://staging.permanent.org/api/"
        const val URL_PROD = "https://www.permanent.org/api/"
        const val URL_UPLOAD_STAGING = "https://staging.permanent.org:9000"
        const val URL_UPLOAD_PROD = "https://www.permanent.org:9000"
        const val URL_PRIVACY_POLICY = "https://www.permanent.org/privacy-policy/"
        const val URL_ADD_STORAGE = "https://www.permanent.org/add-storage/"
        const val MEDIA_TYPE_JSON = "application/json;charset=UTF-8"
        const val AUTH_TYPE_MFA_VALIDATION = "type.auth.mfaValidation"
        const val AUTH_TYPE_PHONE = "type.auth.phone"
        const val ERROR_MFA_TOKEN = "warning.auth.mfaToken"
        const val ERROR_UNKNOWN_SIGNIN = "warning.signin.unknown"
        const val ERROR_SERVER_ERROR = "error.api.invalid_request"
        const val ERROR_NO_API_KEY = "error.api.no_key"
        const val ERROR_ACCOUNT_DUPLICATE = "warning.registration.duplicate_email"
        const val ERROR_PHONE_INVALID = "warning.validation.phone"
        const val ERROR_PASSWORD_COMPLEXITY_LOW = "warning.registration.password_complexity"
        const val ERROR_PASSWORD_NO_MATCH = "warning.registration.password_match"
        const val ERROR_PASSWORD_OLD_INCORRECT = "warning.auth.bad_old_password"
        const val SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED"
        const val FORM_DATA_NAME_THE_FILE = "thefile"
        const val FOLDER_CREATED_PREFIX = "New folder"
        const val FILE_DELETED_SUCCESSFULLY = "Record(s) have been deleted."
        const val FOLDER_DELETED_SUCCESSFULLY = "Folder has been deleted."
        // This is also used in manifest
        const val FILE_PROVIDER_NAME = "org.permanent.permanent.fileprovider"
    }
}