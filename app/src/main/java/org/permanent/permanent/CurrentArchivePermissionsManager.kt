package org.permanent.permanent

import android.content.Context
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class CurrentArchivePermissionsManager private constructor() {

    private var accessRole: AccessRole

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
    }

    fun getAccessRole(): AccessRole = accessRole

    fun isReadAvailable() = accessRole.isReadAvailable()

    fun isCreateAvailable() = accessRole.isCreateAvailable()

    fun isEditAvailable() = accessRole.isEditAvailable()

    fun isDeleteAvailable() = accessRole.isDeleteAvailable()

    fun isMoveAvailable() = accessRole.isMoveAvailable()

    fun isPublishAvailable() = accessRole.isPublishAvailable()

    fun isShareAvailable() = accessRole.isShareAvailable()

    fun isArchiveShareAvailable() = accessRole.isArchiveShareAvailable()

    fun isOwnershipAvailable() = accessRole.isOwnershipAvailable()

    fun getPermissionsEnumerated(): String = accessRole.getPermissionsEnumerated()
}