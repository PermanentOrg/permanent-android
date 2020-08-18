package org.permanent.permanent.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter


object BindingAdapters {

    @BindingAdapter("app:imageResourceId")
    @JvmStatic fun setImageDrawable(view: ImageView, imageDrawableId: Int) {
        view.setImageResource(imageDrawableId)
    }
}
