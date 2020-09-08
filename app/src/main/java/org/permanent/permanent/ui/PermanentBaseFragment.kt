package org.permanent.permanent.ui

import androidx.fragment.app.Fragment

abstract class PermanentBaseFragment : Fragment() {

    abstract fun connectViewModelEvents()

    abstract fun disconnectViewModelEvents()
}