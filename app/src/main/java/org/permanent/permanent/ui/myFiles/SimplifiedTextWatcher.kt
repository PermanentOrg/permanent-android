package org.permanent.permanent.ui.myFiles

import android.text.Editable
import android.text.TextWatcher

interface SimplifiedTextWatcher : TextWatcher {
    override fun beforeTextChanged(var1: CharSequence?, var2: Int, var3: Int, var4: Int) {}
    override fun onTextChanged(
        charSequence: CharSequence,
        start: Int,
        before: Int,
        count: Int
    )
    override fun afterTextChanged(var1: Editable?){}
}