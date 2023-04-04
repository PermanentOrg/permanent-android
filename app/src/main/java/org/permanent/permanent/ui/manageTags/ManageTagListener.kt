package org.permanent.permanent.ui.manageTags

import org.permanent.permanent.models.Tag

interface ManageTagListener {
    fun onTagEditClicked(tag: Tag)
    fun onTagDeleteClicked(tag: Tag)
}