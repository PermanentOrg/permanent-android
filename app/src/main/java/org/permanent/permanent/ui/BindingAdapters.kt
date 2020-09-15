package org.permanent.permanent.ui

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import org.permanent.permanent.R


@BindingAdapter("imageResourceId")
fun setImageDrawable(view: ImageView, imageDrawableId: Int) {
    view.setImageResource(imageDrawableId)
}

@BindingAdapter("imageUrl", "placeholder")
fun loadImage(view: ImageView, url: String?, placeholder: Drawable) {
    if(url == null) {
        view.setImageResource(R.drawable.ic_folder_barney_purple)
    } else {
        Picasso.get().load(url).placeholder(placeholder).into(view)
    }
}

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
