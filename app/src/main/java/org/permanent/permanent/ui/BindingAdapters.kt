package org.permanent.permanent.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import org.permanent.permanent.R
import org.permanent.permanent.models.Notification
import org.permanent.permanent.models.Record
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

@BindingAdapter("record")
fun loadImage(view: ImageView, record: Record?) {
    if (record?.status == null || record.isProcessing) {
        view.setImageResource(R.drawable.ic_processing)
        val rotate = RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 1000
        rotate.repeatCount = Animation.INFINITE
        rotate.repeatMode = Animation.RESTART
        rotate.fillAfter = true
        rotate.interpolator = LinearInterpolator()
        view.startAnimation(rotate)
    } else {
        when (record.type) {
            RecordType.FOLDER -> view.setImageResource(R.drawable.ic_folder_barney_purple)
            else -> Picasso.get()
                .load(record.thumbURL500)
                .placeholder(R.drawable.ic_stop_light_grey)
                .fit()
                .into(view)
        }
    }
}

@BindingAdapter("imageUrl")
fun loadSimpleImage(view: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_stop_light_grey)
        .into(view)
}

@BindingAdapter("roundedImageUrl")
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
    settings.allowContentAccess = true
    settings.allowFileAccess = true
    setBackgroundColor(Color.BLACK)
    if (isVideo == true) {
        loadDataWithBaseURL(
            path,
            "“<html><body>\n" +
                    "<video controls>\n" +
                    "<source src=\\“$path\\” type=\\“video/mp4\\“>\n" +
                    "</video>\n" +
                    "</body></html>”",
            "video/mp4",
            null,
            null
        )
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                loadUrl("javascript:(function() { document.getElementsByTagName('video')[0].pause(); })()")
            }
        }
    } else {
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        path?.let {
            loadUrl(path)
        }
    }
}
