package org.permanent.permanent.models

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("setError")
fun setInputLayoutError(view: TextInputLayout, message: String?) {
    if (message != null)
    {
        if(view.error != message)
        view.error = message
    }
    else{
        view.isErrorEnabled=false
    }
}