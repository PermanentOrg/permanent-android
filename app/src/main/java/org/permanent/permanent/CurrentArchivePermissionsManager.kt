package org.permanent.permanent

import android.content.Context
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class CurrentArchivePermissionsManager private constructor() {

    private var accessRole: AccessRole
    private var isReadAvailable: Boolean = false
    private var isCreateAvailable: Boolean = false
    private var isEditAvailable: Boolean = false
    private var isDeleteAvailable: Boolean = false
    private var isMoveAvailable: Boolean = false
    private var isPublishAvailable: Boolean = false
    private var isShareAvailable: Boolean = false
    private var isArchiveShareAvailable: Boolean = false
    private var isOwnershipAvailable: Boolean = false

    companion object {
        val instance = CurrentArchivePermissionsManager()
    }

    init {
        val prefsHelper = PreferencesHelper(
            PermanentApplication.instance.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        )
        accessRole = prefsHelper.getCurrentArchiveAccessRole()
        onAccessRoleChanged(accessRole)
    }

    fun onAccessRoleChanged(accessRole: AccessRole) {
        this.accessRole = accessRole
        when (accessRole) {
            AccessRole.OWNER -> {
                isReadAvailable = true
                isCreateAvailable = true
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

    fun getAccessRole(): AccessRole = accessRole

    fun isReadAvailable() = isReadAvailable

    fun isCreateAvailable() = isCreateAvailable

    fun isEditAvailable() = isEditAvailable

    fun isDeleteAvailable() = isDeleteAvailable

    fun isMoveAvailable() = isMoveAvailable

    fun isPublishAvailable() = isPublishAvailable

    fun isShareAvailable() = isShareAvailable

    fun isArchiveShareAvailable() = isArchiveShareAvailable

    fun isOwnershipAvailable() = isOwnershipAvailable
}