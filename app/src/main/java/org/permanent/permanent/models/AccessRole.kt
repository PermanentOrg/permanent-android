package org.permanent.permanent.models

enum class AccessRole(private val backendString: String) {
    OWNER("access.role.owner"),
    MANAGER("access.role.manager"),
    CURATOR("access.role.curator"),
    EDITOR("access.role.editor"),
    CONTRIBUTOR("access.role.contributor"),
    VIEWER("access.role.viewer");

    fun toTitleCase(): String {
        return this.name.toLowerCase().capitalize()
    }

    fun toLowerCase(): String {
        return this.name.toLowerCase()
    }

    fun toBackendString(): String {
        return backendString
    }
}