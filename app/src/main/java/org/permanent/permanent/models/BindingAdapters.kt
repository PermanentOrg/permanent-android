package org.permanent.permanent.models

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_login.*
import org.permanent.R

@BindingAdapter("setError")
fun setInputLayoutError(view: TextInputLayout, messageId: Int?) {

    if (messageId != null) {
        val message = view.context.getString(messageId)
        if (view.error != message)
            view.error = message
        view.requestFocus()
    } else {
        view.isErrorEnabled = false
    }
}