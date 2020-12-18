package org.permanent.permanent.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import org.permanent.permanent.R
import org.permanent.permanent.models.RecordType


@BindingAdapter("imageResourceId")
fun setImageDrawable(view: ImageView, imageDrawableId: Int) {
    view.setImageResource(imageDrawableId)
}

@BindingAdapter("fileType", "imageUrl")
fun loadImage(view: ImageView, fileType: RecordType, url: String?) {
    when (fileType) {
        RecordType.FOLDER -> view.setImageResource(R.drawable.ic_folder_barney_purple)
        else -> Picasso.get()
            .load(url)
            .placeholder(R.drawable.ic_stop_light_grey)
            .fit()
            .into(view)
    }
}

@BindingAdapter("imageUrl")
fun loadRoundedImage(view: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_stop_light_grey)
        .fit()
        .transform(CircleTransform())
        .into(view)
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
