package org.permanent.permanent.ui

import android.content.SharedPreferences
import androidx.window.core.layout.WindowWidthSizeClass
import org.permanent.permanent.CurrentArchivePermissionsManager
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.network.models.TwoFAVO

const val PREFS_NAME = "permanent_preferences"
const val IS_USER_LOGGED_IN = "is_user_logged_in"
const val IS_BIOMETRICS_LOG_IN = "is_biometrics_log_in"
const val IS_LIST_VIEW_MODE = "is_list_view_mode"
const val IS_TWO_FA_ENABLED = "is_two_fa_enabled"
const val SHOW_ARCHIVES_SCREEN = "should_show_archives_screen"
const val SHOW_REDEEM_CODE_SCREEN = "should_redeem_code_screen"
const val PROMO_CODE = "promo_code"
const val WINDOW_WIDTH_SIZE_CLASS = "window_width_size_class"
const val PREFS_ACCOUNT_ID = "preferences_user_account_id"
const val PREFS_PUBLIC_RECORD_FOLDER_ID = "preferences_public_record_folder_id"
const val PREFS_PUBLIC_RECORD_FOLDER_LINK_ID = "preferences_public_record_folder_link_id"
const val PREFS_PUBLIC_RECORD_ARCHIVE_NR = "preferences_public_record_archive_nr"
const val PREFS_PUBLIC_RECORD_THUMB_URL_2000 = "preferences_public_record_thumb_url_2000"
const val PREFS_ACCOUNT_EMAIL = "preferences_user_email"
const val PREFS_ACCOUNT_PASSWORD = "preferences_user_password"
const val PREFS_ACCOUNT_NAME = "preferences_user_name"
const val PREFS_ACCOUNT_HIDE_CHECKLIST = "preferences_hide_checklist"
const val PREFS_DEFAULT_ARCHIVE_ID = "preferences_default_archive_id"
const val PREFS_CURRENT_ARCHIVE_ID = "preferences_current_archive_id"
const val PREFS_CURRENT_ARCHIVE_NUMBER = "preferences_current_archive_number"
const val PREFS_CURRENT_ARCHIVE_TYPE = "preferences_current_archive_type"
const val PREFS_CURRENT_ARCHIVE_FULL_NAME = "preferences_current_archive_full_name"
const val PREFS_CURRENT_ARCHIVE_THUMB_URL = "preferences_current_archive_thumb_url"
const val PREFS_CURRENT_ARCHIVE_ACCESS_ROLE = "preferences_current_archive_access_role"
const val PREFS_SHARE_LINK_URL_TOKEN = "preferences_share_link_url_token"
const val PREFS_DEEP_LINK_ARCHIVE_NR = "preferences_deep_link_archive_nr"
const val PREFS_AUTH_TOKEN = "preferences_auth_token"
const val PREFS_UPLOAD_URL = "preferences_upload_url"
const val PREFS_DEEP_LINK_FILE_ARCHIVE_NR = "preferences_deep_link_file_archive_nr"
const val PREFS_DEEP_LINK_FOLDER_ARCHIVE_NR = "preferences_deep_link_folder_archive_nr"
const val PREFS_DEEP_LINK_FOLDER_LINK_ID = "preferences_deep_link_folder_link_id"
const val KEY_TWO_FA_LIST = "key_two_fa_list"
const val KEY_CHECKLIST_TOOLTIP_SHOWN = "key_checklist_tooltip_shown"

class PreferencesHelper(private val sharedPreferences: SharedPreferences) {

    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(IS_USER_LOGGED_IN, isLoggedIn)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_USER_LOGGED_IN, false)
    }

    fun setBiometricsLogIn(isBiometricsLogIn: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(IS_BIOMETRICS_LOG_IN, isBiometricsLogIn)
            apply()
        }
    }

    fun isBiometricsLogIn(): Boolean {
        return sharedPreferences.getBoolean(IS_BIOMETRICS_LOG_IN, true)
    }

    fun saveAccountInfo(id: Int?, email: String?, password: String?, name: String?) {
        id?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_ACCOUNT_ID, it)
                apply()
            }
        }
        email?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_ACCOUNT_EMAIL, it)
                apply()
            }
        }
        password?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_ACCOUNT_PASSWORD, it)
                apply()
            }
        }
        name?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_ACCOUNT_NAME, it)
                apply()
            }
        }
    }

    fun saveAccountEmail(email: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_ACCOUNT_EMAIL, email)
            apply()
        }
    }

    fun getAccountId(): Int {
        return sharedPreferences.getInt(PREFS_ACCOUNT_ID, 0)
    }

    fun getAccountEmail(): String? {
        return sharedPreferences.getString(PREFS_ACCOUNT_EMAIL, "")
    }

    fun getAccountPassword(): String? {
        return sharedPreferences.getString(PREFS_ACCOUNT_PASSWORD, "")
    }

    fun getAccountName(): String? {
        return sharedPreferences.getString(PREFS_ACCOUNT_NAME, "")
    }

    fun getAccountHideChecklist(): Boolean {
        return sharedPreferences.getBoolean(PREFS_ACCOUNT_HIDE_CHECKLIST, false)
    }

    fun saveAccountHideChecklist(hideChecklist: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(PREFS_ACCOUNT_HIDE_CHECKLIST, hideChecklist)
            apply()
        }
    }

    fun saveDefaultArchiveId(id: Int?) {
        with(sharedPreferences.edit()) {
            id?.let {
                putInt(PREFS_DEFAULT_ARCHIVE_ID, id)
            } ?: putInt(PREFS_DEFAULT_ARCHIVE_ID, 0)
            apply()
        }
    }

    fun getDefaultArchiveId(): Int {
        return sharedPreferences.getInt(PREFS_DEFAULT_ARCHIVE_ID, 0)
    }

    fun saveCurrentArchiveInfo(
        id: Int?,
        number: String?,
        type: ArchiveType?,
        name: String?,
        thumbURL: String?,
        accessRole: AccessRole?
    ) {
        id?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_CURRENT_ARCHIVE_ID, id)
                apply()
            }
        }
        number?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_NUMBER, number)
                apply()
            }
        }
        type?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_TYPE, type.backendString)
                apply()
            }
        }
        name?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_FULL_NAME, name)
                apply()
            }
        }
        with(sharedPreferences.edit()) {
            putString(PREFS_CURRENT_ARCHIVE_THUMB_URL, thumbURL)
            apply()
        }
        accessRole?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_ACCESS_ROLE, accessRole.backendString)
                apply()
            }
            CurrentArchivePermissionsManager.instance.onAccessRoleChanged(accessRole)
        }
    }

    fun updateCurrentArchiveName(name: String?) {
        name?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_FULL_NAME, "The $name Archive")
                apply()
            }
        }
    }

    fun updateCurrentArchiveThumbURL(thumbURL: String?) {
        with(sharedPreferences.edit()) {
            putString(PREFS_CURRENT_ARCHIVE_THUMB_URL, thumbURL)
            apply()
        }
    }

    fun getCurrentArchive(): Archive {
        return Archive(
            getCurrentArchiveId(),
            getCurrentArchiveNr(),
            getCurrentArchiveType(),
            getCurrentArchiveFullName(),
            getCurrentArchiveThumbURL(),
            getCurrentArchiveAccessRole(),
        )
    }

    fun getCurrentArchiveId(): Int {
        return sharedPreferences.getInt(PREFS_CURRENT_ARCHIVE_ID, 0)
    }

    fun getCurrentArchiveNr(): String? {
        return sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_NUMBER, "")
    }

    fun getCurrentArchiveType(): ArchiveType {
        return when (sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_TYPE, "")) {
            ArchiveType.FAMILY.backendString -> ArchiveType.FAMILY
            ArchiveType.ORGANIZATION.backendString -> ArchiveType.ORGANIZATION
            else -> ArchiveType.PERSON
        }
    }

    fun getCurrentArchiveFullName(): String? {
        return sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_FULL_NAME, "")
    }

    fun getCurrentArchiveThumbURL(): String? {
        return sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_THUMB_URL, null)
    }

    fun getCurrentArchiveAccessRole(): AccessRole {
        return when (sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_ACCESS_ROLE, "")) {
            AccessRole.OWNER.backendString -> AccessRole.OWNER
            AccessRole.MANAGER.backendString -> AccessRole.MANAGER
            AccessRole.CURATOR.backendString -> AccessRole.CURATOR
            AccessRole.EDITOR.backendString -> AccessRole.EDITOR
            AccessRole.CONTRIBUTOR.backendString -> AccessRole.CONTRIBUTOR
            else -> AccessRole.VIEWER
        }
    }

    fun savePublicRecordInfo(
        folderId: Int?, folderLinkId: Int?, archiveNr: String?, thumbURL2000: String?
    ) {
        folderId?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_PUBLIC_RECORD_FOLDER_ID, it)
                apply()
            }
        }
        folderLinkId?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_PUBLIC_RECORD_FOLDER_LINK_ID, it)
                apply()
            }
        }
        archiveNr?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_PUBLIC_RECORD_ARCHIVE_NR, it)
                apply()
            }
        }
        thumbURL2000?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_PUBLIC_RECORD_THUMB_URL_2000, it)
                apply()
            }
        }
    }

    fun updatePublicRecordThumbURL(thumbURL2000: String?) {
        thumbURL2000?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_PUBLIC_RECORD_THUMB_URL_2000, it)
                apply()
            }
        }
    }

    fun getPublicRecordFolderId(): Int {
        return sharedPreferences.getInt(PREFS_PUBLIC_RECORD_FOLDER_ID, 0)
    }

    fun getPublicRecordFolderLinkId(): Int {
        return sharedPreferences.getInt(PREFS_PUBLIC_RECORD_FOLDER_LINK_ID, 0)
    }

    fun getPublicRecordArchiveNr(): String? {
        return sharedPreferences.getString(PREFS_PUBLIC_RECORD_ARCHIVE_NR, "")
    }

    fun getPublicRecordThumbURL2000(): String? {
        return sharedPreferences.getString(PREFS_PUBLIC_RECORD_THUMB_URL_2000, "")
    }

    fun saveShareLinkUrlToken(urlToken: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_SHARE_LINK_URL_TOKEN, urlToken)
            apply()
        }
    }

    fun getShareLinkUrlToken(): String? {
        return sharedPreferences.getString(PREFS_SHARE_LINK_URL_TOKEN, "")
    }

    fun saveDeepLinkArchiveNr(archiveNr: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_DEEP_LINK_ARCHIVE_NR, archiveNr)
            apply()
        }
    }

    fun getDeepLinkArchiveNr(): String? {
        return sharedPreferences.getString(PREFS_DEEP_LINK_ARCHIVE_NR, "")
    }

    fun saveDeepLinkFileArchiveNr(fileArchiveNr: String?) {
        with(sharedPreferences.edit()) {
            putString(PREFS_DEEP_LINK_FILE_ARCHIVE_NR, fileArchiveNr)
            apply()
        }
    }

    fun getDeepLinkFileArchiveNr(): String? {
        return sharedPreferences.getString(PREFS_DEEP_LINK_FILE_ARCHIVE_NR, null)
    }

    fun saveDeepLinkFolderArchiveNr(folderArchiveNr: String?) {
        with(sharedPreferences.edit()) {
            putString(PREFS_DEEP_LINK_FOLDER_ARCHIVE_NR, folderArchiveNr)
            apply()
        }
    }

    fun getDeepLinkFolderArchiveNr(): String? {
        return sharedPreferences.getString(PREFS_DEEP_LINK_FOLDER_ARCHIVE_NR, null)
    }

    fun saveDeepLinkFolderLinkId(folderLinkId: String?) {
        with(sharedPreferences.edit()) {
            putString(PREFS_DEEP_LINK_FOLDER_LINK_ID, folderLinkId)
            apply()
        }
    }

    fun getDeepLinkFolderLinkId(): String? {
        return sharedPreferences.getString(PREFS_DEEP_LINK_FOLDER_LINK_ID, null)
    }

    fun saveIsListViewMode(isListViewMode: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(IS_LIST_VIEW_MODE, isListViewMode)
            apply()
        }
    }

    fun isListViewMode(): Boolean {
        return sharedPreferences.getBoolean(IS_LIST_VIEW_MODE, true)
    }

    fun saveAuthToken(token: String?) {
        with(sharedPreferences.edit()) {
            putString(PREFS_AUTH_TOKEN, token)
            apply()
        }
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(PREFS_AUTH_TOKEN, "")
    }

    fun getUploadURL(): String? {
        return sharedPreferences.getString(PREFS_UPLOAD_URL, "")
    }

    fun saveUploadURL(url: String?) {
        with(sharedPreferences.edit()) {
            putString(PREFS_UPLOAD_URL, url)
            apply()
        }
    }

    fun saveShowArchivesDeepLink(shouldShow: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(SHOW_ARCHIVES_SCREEN, shouldShow)
            apply()
        }
    }

    fun showArchivesScreen(): Boolean {
        return sharedPreferences.getBoolean(SHOW_ARCHIVES_SCREEN, false)
    }

    fun saveShowRedeemCodeDeepLink(shouldShow: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(SHOW_REDEEM_CODE_SCREEN, shouldShow)
            apply()
        }
    }

    fun showRedeemCodeScreen(): Boolean {
        return sharedPreferences.getBoolean(SHOW_REDEEM_CODE_SCREEN, false)
    }

    fun savePromoCodeFromDeepLink(code: String) {
        with(sharedPreferences.edit()) {
            putString(PROMO_CODE, code)
            apply()
        }
    }

    fun getPromoCode(): String? {
        return sharedPreferences.getString(PROMO_CODE, "")
    }

    fun saveWindowWidthSizeClass(windowWidthSizeClass: WindowWidthSizeClass) {
        with(sharedPreferences.edit()) {
            putString(WINDOW_WIDTH_SIZE_CLASS, windowWidthSizeClass.toString())
            apply()
        }
    }

    fun isTablet(): Boolean {
        val windowWidthSizeString = sharedPreferences.getString(WINDOW_WIDTH_SIZE_CLASS, "")
        return !windowWidthSizeString.equals(WindowWidthSizeClass.COMPACT.toString())
    }

    fun setIsTwoFAEnabled(isEnabled: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(IS_TWO_FA_ENABLED, isEnabled)
            apply()
        }
    }

    fun isTwoFAEnabled(): Boolean {
        return sharedPreferences.getBoolean(IS_TWO_FA_ENABLED, false)
    }

    fun setTwoFAList(twoFAList: List<TwoFAVO>) {
        with(sharedPreferences.edit()) {
            putString(KEY_TWO_FA_LIST, twoFAList.toJson())
            apply()
        }
    }

    fun getTwoFAList(): List<TwoFAVO> {
        val json = sharedPreferences.getString(KEY_TWO_FA_LIST, null) ?: return emptyList()
        return json.toTwoFAVOList().sortedByDescending { it.method == "sms" }
    }

    fun setChecklistTooltipShown(shown: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_CHECKLIST_TOOLTIP_SHOWN, shown)
            apply()
        }
    }

    fun isChecklistTooltipShown(): Boolean {
        return sharedPreferences.getBoolean(KEY_CHECKLIST_TOOLTIP_SHOWN, false)
    }
}