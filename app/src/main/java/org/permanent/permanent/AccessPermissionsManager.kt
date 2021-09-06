package org.permanent.permanent

import android.content.Context
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class AccessPermissionsManager private constructor() {

    private var isReadAvailable: Boolean = false
    private var isCreateAvailable: Boolean = false
    private var isUploadAvailable: Boolean = false
    private var isEditAvailable: Boolean = false
    private var isDeleteAvailable: Boolean = false
    private var isMoveAvailable: Boolean = false
    private var isPublishAvailable: Boolean = false
    private var isShareAvailable: Boolean = false
    private var isArchiveShareAvailable: Boolean = false
    private var isOwnershipAvailable: Boolean = false

    companion object {
        val instance = AccessPermissionsManager()
    }

    init {
        val prefsHelper = PreferencesHelper(
            PermanentApplication.instance.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        )

        onAccessRoleChanged(prefsHelper.getCurrentArchiveAccessRole())
    }

    fun onAccessRoleChanged(accessRole: AccessRole) {
        when (accessRole) {
            AccessRole.OWNER -> {
                isReadAvailable = true
                isCreateAvailable = true
                isUploadAvailable = true
                isEditAvailable = true
                isDeleteAvailable = true
                isMoveAvailable = true
                isPublishAvailable = true
                isShareAvailable = true
                isArchiveShareAvailable = true
                isOwnershipAvailable = true
            }
            AccessRole.MANAGER -> {
                isReadAvailable = true
                isCreateAvailable = true
                isUploadAvailable = true
                isEditAvailable = true
                isDeleteAvailable = true
                isMoveAvailable = true
                isPublishAvailable = true
                isShareAvailable = true
                isArchiveShareAvailable = true
                isOwnershipAvailable = false
            }
            AccessRole.CURATOR -> {
                isReadAvailable = true
                isCreateAvailable = true
                isUploadAvailable = true
                isEditAvailable = true
                isDeleteAvailable = true
                isMoveAvailable = true
                isPublishAvailable = true
                isShareAvailable = true
                isArchiveShareAvailable = false
                isOwnershipAvailable = false
            }
            AccessRole.EDITOR -> {
                isReadAvailable = true
                isCreateAvailable = true
                isUploadAvailable = true
                isEditAvailable = true
                isDeleteAvailable = false
                isMoveAvailable = false
                isPublishAvailable = false
                isShareAvailable = false
                isArchiveShareAvailable = false
                isOwnershipAvailable = false
            }
            AccessRole.CONTRIBUTOR -> {
                isReadAvailable = true
                isCreateAvailable = true
                isUploadAvailable = true
                isEditAvailable = false
                isDeleteAvailable = false
                isMoveAvailable = false
                isPublishAvailable = false
                isShareAvailable = false
                isArchiveShareAvailable = false
                isOwnershipAvailable = false
            }
            AccessRole.VIEWER -> {
                isReadAvailable = true
                isCreateAvailable = false
                isUploadAvailable = false
                isEditAvailable = false
                isDeleteAvailable = false
                isMoveAvailable = false
                isPublishAvailable = false
                isShareAvailable = false
                isArchiveShareAvailable = false
                isOwnershipAvailable = false
            }
        }
    }

    fun isReadAvailable() = isReadAvailable

    fun isCreateAvailable() = isCreateAvailable

    fun isUploadAvailable() = isUploadAvailable

    fun isEditAvailable() = isEditAvailable

    fun isDeleteAvailable() = isDeleteAvailable

    fun isMoveAvailable() = isMoveAvailable

    fun isPublishAvailable() = isPublishAvailable

    fun isShareAvailable() = isShareAvailable

    fun isArchiveShareAvailable() = isArchiveShareAvailable

    fun isOwnershipAvailable() = isOwnershipAvailable
}