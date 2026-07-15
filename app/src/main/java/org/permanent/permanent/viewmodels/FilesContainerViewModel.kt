package org.permanent.permanent.viewmodels

import android.app.Application

class FilesContainerViewModel(application: Application) : ObservableAndroidViewModel(application) {

    /**
     * Last pager position; the pager's own state restore is disabled (it fights the
     * adapter recreated on every view recreation), so this survives info->back and
     * configuration changes instead. -1 = not set, open at the record tapped in the list.
     */
    var currentFilePosition = -1
}