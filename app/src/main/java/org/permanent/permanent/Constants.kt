package org.permanent.permanent

class Constants {
    companion object {
        const val POSITION_SHARED_BY_ME_FRAGMENT = 0
        const val POSITION_SHARED_WITH_ME_FRAGMENT = 1
        const val POSITION_INFO_FRAGMENT = 0
        const val POSITION_DETAILS_FRAGMENT = 1
        const val REQUEST_CODE_FILE_SELECT = 200
        const val REQUEST_CODE_IMAGE_CAPTURE = 201
        const val REQUEST_CODE_VIDEO_CAPTURE = 203
        const val REQUEST_CODE_GOOGLE_API_AVAILABILITY = 2404
        const val LOGIN_URL_SUFFIX = "auth/login"
        const val SIGN_UP_URL_SUFFIX = "account/post"
        const val S3_BASE_URL = "https://s3.us-west-2.amazonaws.com"
        const val MY_FILES_FOLDER = "My Files"
        const val PUBLIC_FILES_FOLDER = "Public"
        const val PRIVATE_FILES = "Private Files"
        const val PUBLIC_FILES = "Public Files"
        const val MEDIA_TYPE_JSON = "application/json;charset=UTF-8"
        const val MEDIA_TYPE_OCTET_STREAM = "application/octet-stream"
        const val AUTH_TYPE_MFA_VALIDATION = "type.auth.mfaValidation"
        const val AUTH_TYPE_PHONE = "type.auth.phone"
        const val AUTH_REQUEST_SCOPE = "offline_access"
        const val ERROR_MFA_TOKEN = "warning.auth.mfaToken"
        const val ERROR_INVALID_VERIFICATION_CODE = "warning.auth.token_does_not_match"
        const val ERROR_EXPIRED_VERIFICATION_CODE = "warning.auth.token_expired"
        const val ERROR_UNKNOWN_SIGNIN = "warning.signin.unknown"
        const val ERROR_SERVER_ERROR = "error.api.invalid_request"
        const val ERROR_OWNER_ALREADY_PENDING = "error.pr.pending_owner"
        const val ERROR_PENDING_OWNER_NOT_EDITABLE = "error.pr.update_share_owner"
        const val ERROR_MEMBER_ALREADY_ADDED = "error.pr.duplicate_share"
        const val ERROR_NO_API_KEY = "error.api.no_key"
        const val ERROR_ACCOUNT_DUPLICATE = "warning.registration.duplicate_email"
        const val ERROR_PHONE_INVALID = "warning.validation.phone"
        const val ERROR_PASSWORD_COMPLEXITY_LOW = "warning.registration.password_complexity"
        const val ERROR_PASSWORD_NO_MATCH = "warning.registration.password_match"
        const val ERROR_PASSWORD_OLD_INCORRECT = "warning.auth.bad_old_password"
        const val SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED"
        const val FILE_DELETED_SUCCESSFULLY = "Record(s) have been deleted."
        const val FOLDER_DELETED_SUCCESSFULLY = "Folder has been deleted."
        // This is also used in manifest
        const val FILE_PROVIDER_NAME = ".fileprovider"
    }
}