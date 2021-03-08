package org.permanent.permanent.models

enum class FileType {
    VIDEO, IMAGE, PDF, UNKNOWN;

    override fun toString(): String {
        return super.toString().toLowerCase()
    }
}