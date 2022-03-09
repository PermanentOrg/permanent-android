package org.permanent.permanent.ui.public

import org.permanent.permanent.models.ProfileItem

interface ProfileItemListener {
    fun onOptionsClick(profileItem: ProfileItem)
    fun onEditClick(profileItem: ProfileItem)
    fun onDeleteClick(profileItem: ProfileItem)
}