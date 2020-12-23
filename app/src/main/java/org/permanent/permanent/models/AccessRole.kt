package org.permanent.permanent.models

enum class AccessRole {
    OWNER, MANAGER, CURATOR, EDITOR, CONTRIBUTOR, VIEWER;

    fun toTitleCase(): String {
        return this.name.toLowerCase().capitalize()
    }

    fun toLowerCase(): String {
        return this.name.toLowerCase()
    }
}