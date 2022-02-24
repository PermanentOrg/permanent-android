package org.permanent.permanent.ui.publicWorkspace

import org.permanent.permanent.models.ProfileItem

interface OnlinePresenceListener {
    fun onOptionsClick(profileItem: ProfileItem)
    fun onEditClick(profileItem: ProfileItem)
    fun onDeleteClick(profileItem: ProfileItem)
}