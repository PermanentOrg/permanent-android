package org.permanent.permanent.models

data class File(
    val name: String,
    val type: String,
    val date: String,
    val shared: Boolean,
    val thumbnails: List<String>)