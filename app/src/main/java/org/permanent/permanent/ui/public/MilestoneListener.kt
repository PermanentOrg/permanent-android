package org.permanent.permanent.ui.public

import org.permanent.permanent.models.Milestone

interface MilestoneListener {
    fun onOptionsClick(milestone: Milestone)
    fun onEditClick(milestone: Milestone)
    fun onDeleteClick(milestone: Milestone)
}