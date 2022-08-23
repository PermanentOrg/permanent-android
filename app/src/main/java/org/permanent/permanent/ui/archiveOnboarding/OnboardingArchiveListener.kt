package org.permanent.permanent.ui.archiveOnboarding

import org.permanent.permanent.models.Archive

interface OnboardingArchiveListener {
    fun onAcceptBtnClick(archive: Archive)
    fun onMakeDefaultBtnClick(archive: Archive)
}