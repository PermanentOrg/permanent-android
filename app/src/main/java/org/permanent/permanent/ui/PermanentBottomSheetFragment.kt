package org.permanent.permanent.ui

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class PermanentBottomSheetFragment : BottomSheetDialogFragment() {

    abstract fun connectViewModelEvents()

    abstract fun disconnectViewModelEvents()
}
