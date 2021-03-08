package org.permanent.permanent.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import org.permanent.permanent.R
import org.permanent.permanent.models.Notification
import org.permanent.permanent.models.RecordType


@BindingAdapter("imageResourceId")
fun setImageDrawable(view: ImageView, imageDrawableId: Int) {
    view.setImageResource(imageDrawableId)
}

@BindingAdapter("notificationTypeIcon")
fun setIconDrawable(view: ImageView, notificationType: Notification.Type) {
    when (notificationType) {
        Notification.Type.SHARE -> view.setImageResource(R.drawable.ic_notification_folder_shared_tangerine)
        Notification.Type.RELATIONSHIP -> view.setImageResource(R.drawable.ic_notification_group_deep_red)
        else -> view.setImageResource(R.drawable.ic_notification_account_blue)
    }
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

@SuppressLint("SetJavaScriptEnabled")
@BindingAdapter("webViewPath", "isVideo")
fun WebView.updatePath(path: String?, isVideo: Boolean?) {
    settings.javaScriptEnabled = true
    settings.loadWithOverviewMode = true
    settings.useWideViewPort = true
    if (isVideo == true) {
        loadDataWithBaseURL(
            path,
            "“<html><body>\n" +
                    "<video controls autoplay>\n" +
                    "<source src=\\“$path\\” type=\\“video/mp4\\“>\n" +
                    "</video>\n" +
                    "</body></html>”",
            "video/mp4",
            null,
            null)
    } else {
        path?.let {
            loadUrl(path)
        }
    }
}
