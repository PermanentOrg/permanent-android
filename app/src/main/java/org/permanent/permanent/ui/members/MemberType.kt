package org.permanent.permanent.ui.members

enum class MemberType {
    OWNER, CURATOR, EDITOR, CONTRIBUTOR, VIEWER;

    fun toTitleCase(): String {
        return this.name.toLowerCase().capitalize()
    }
}