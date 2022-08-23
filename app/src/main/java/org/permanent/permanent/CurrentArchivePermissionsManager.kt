package org.permanent.permanent

import android.content.Context
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class CurrentArchivePermissionsManager private constructor() {

    private var accessRole: AccessRole
    private var currentArchivePermissions = mutableListOf<ArchivePermission>()

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
                currentArchivePermissions = mutableListOf(
                    ArchivePermission.READ,
                    ArchivePermission.CREATE,
                    ArchivePermission.EDIT,
                    ArchivePermission.DELETE,
                    ArchivePermission.MOVE,
                    ArchivePermission.PUBLISH,
                    ArchivePermission.SHARE,
                    ArchivePermission.ARCHIVE_SHARE,
                    ArchivePermission.OWNERSHIP
                )
            }
            AccessRole.MANAGER -> {
                currentArchivePermissions = mutableListOf(
                    ArchivePermission.READ,
                    ArchivePermission.CREATE,
                    ArchivePermission.EDIT,
                    ArchivePermission.DELETE,
                    ArchivePermission.MOVE,
                    ArchivePermission.PUBLISH,
                    ArchivePermission.SHARE,
                    ArchivePermission.ARCHIVE_SHARE
                )
            }
            AccessRole.CURATOR -> {
                currentArchivePermissions = mutableListOf(
                    ArchivePermission.READ,
                    ArchivePermission.CREATE,
                    ArchivePermission.EDIT,
                    ArchivePermission.DELETE,
                    ArchivePermission.MOVE,
                    ArchivePermission.PUBLISH,
                    ArchivePermission.SHARE
                )
            }
            AccessRole.EDITOR -> {
                currentArchivePermissions = mutableListOf(
                    ArchivePermission.READ,
                    ArchivePermission.CREATE,
                    ArchivePermission.EDIT
                )
            }
            AccessRole.CONTRIBUTOR -> {
                currentArchivePermissions = mutableListOf(
                    ArchivePermission.READ,
                    ArchivePermission.CREATE
                )
            }
            AccessRole.VIEWER -> {
                currentArchivePermissions = mutableListOf(
                    ArchivePermission.READ
                )
            }
        }
    }

    fun getAccessRole(): AccessRole = accessRole

    fun isReadAvailable() = currentArchivePermissions.contains(ArchivePermission.READ)

    fun isCreateAvailable() = currentArchivePermissions.contains(ArchivePermission.CREATE)

    fun isEditAvailable() = currentArchivePermissions.contains(ArchivePermission.EDIT)

    fun isDeleteAvailable() = currentArchivePermissions.contains(ArchivePermission.DELETE)

    fun isMoveAvailable() = currentArchivePermissions.contains(ArchivePermission.MOVE)

    fun isPublishAvailable() = currentArchivePermissions.contains(ArchivePermission.PUBLISH)

    fun isShareAvailable() = currentArchivePermissions.contains(ArchivePermission.SHARE)

    fun isArchiveShareAvailable() =
        currentArchivePermissions.contains(ArchivePermission.ARCHIVE_SHARE)

    fun isOwnershipAvailable() = currentArchivePermissions.contains(ArchivePermission.OWNERSHIP)

    fun getPermissionsEnumerated(): String {
        var enumeratedPermissions = ""
        currentArchivePermissions.remove(ArchivePermission.ARCHIVE_SHARE)
        currentArchivePermissions.map { it.toLowerCase() }
        currentArchivePermissions.forEachIndexed { index, permission ->
            enumeratedPermissions += when {
                currentArchivePermissions.size == 1 -> {
                    permission.toUIString()
                }
                index != currentArchivePermissions.size - 1 -> {
                    "${permission.toUIString()}, "
                }
                else -> {
                    "and ${permission.toUIString()}"
                }
            }
        }
        return enumeratedPermissions
    }
}