package org.permanent.permanent.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.models.Notification
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.myFiles.saveToPermanent.ResizeTransformation

@BindingAdapter("app:icon")
fun setButtonIcon(button: Button, drawableRes: Drawable) {
    button.setCompoundDrawablesWithIntrinsicBounds(drawableRes, null, null, null)
}

@BindingAdapter("imageResourceId")
fun setImageDrawable(view: ImageView, imageDrawableId: Int) {
    view.setImageResource(imageDrawableId)
}

@BindingAdapter("notificationTypeIcon")
fun setNotificationIcon(view: ImageView, notificationType: Notification.Type) {
    when (notificationType) {
        Notification.Type.SHARE -> view.setImageResource(R.drawable.ic_notification_folder_shared_tangerine)
        Notification.Type.RELATIONSHIP -> view.setImageResource(R.drawable.ic_notification_group_deep_red)
        else -> view.setImageResource(R.drawable.ic_notification_account_blue)
    }
}

@BindingAdapter("archiveTypeIcon")
fun setArchiveTypeIcon(view: ImageView, archiveType: ArchiveType?) {
    when (archiveType) {
        ArchiveType.FAMILY -> view.setImageResource(R.drawable.ic_group_primary)
        ArchiveType.ORGANIZATION -> view.setImageResource(R.drawable.ic_organization_filled_primary)
        else -> view.setImageResource(R.drawable.ic_account_primary)
    }
}

@BindingAdapter("viewModeIcon")
fun setViewModeIconDrawable(view: ImageView, isListViewMode: Boolean) {
    if (isListViewMode) view.setImageResource(R.drawable.ic_grid_dark_blue)
    else view.setImageResource(R.drawable.ic_list_dark_blue)
}

@BindingAdapter("record")
fun loadImage(view: ImageView, record: Record?) {
    if (record?.isProcessing == true) {
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
        if (record == null) {
            view.setImageResource(R.drawable.ic_copy)
        } else {
            when (record.type) {
                RecordType.FOLDER -> view.setImageResource(R.drawable.ic_folder_barney_purple)
                else -> Picasso.get()
                    .load(record.thumbURL200)
                    .placeholder(R.drawable.ic_stop_light_grey)
                    .into(view)
            }
        }
    }
}

@BindingAdapter("imageUrl")
fun loadUrl(view: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_stop_light_grey)
        .into(view)
}

@BindingAdapter("imageUri")
fun loadUri(view: ImageView, uri: Uri) {
    Picasso.get()
        .load(uri)
        .placeholder(R.drawable.ic_file_light_grey)
        .transform(ResizeTransformation(1024))
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

@BindingAdapter("showFabAddAnimated")
fun showFabAddAnimated(fabAdd: FloatingActionButton, show: Boolean) {
    val parent = fabAdd.parent as? ConstraintLayout ?: return
    val checklist = parent.findViewById<FloatingActionButton>(R.id.fabChecklist)
    val offset = 300f
    val duration = 300L

    if (show) {
        fabAdd.post {
            fabAdd.translationY = offset
            fabAdd.alpha = 0f
            fabAdd.visibility = View.VISIBLE

            // Animate into view
            fabAdd.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .start()

            // Update constraint based on checklist visibility
            val constraintSet = ConstraintSet()
            constraintSet.clone(parent)
            constraintSet.clear(R.id.fabAdd, ConstraintSet.BOTTOM)
            if (checklist?.visibility == View.VISIBLE) {
                constraintSet.connect(
                    R.id.fabAdd, ConstraintSet.BOTTOM,
                    R.id.fabChecklist, ConstraintSet.TOP, 16
                )
            } else {
                constraintSet.connect(
                    R.id.fabAdd, ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 32
                )
            }
            TransitionManager.beginDelayedTransition(parent)
            constraintSet.applyTo(parent)
        }
    } else {
        fabAdd.animate()
            .translationY(offset)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                fabAdd.visibility = View.GONE
            }
            .start()
    }
}

@BindingAdapter("showChecklistFabAnimated")
fun showChecklistFabAnimated(fabChecklist: FloatingActionButton, show: Boolean) {
    val parent = fabChecklist.parent as? ConstraintLayout ?: return
    val fabAdd = parent.findViewById<FloatingActionButton>(R.id.fabAdd)
    val offset = 300f
    val duration = 300L

    if (show) {
        fabChecklist.post {
            fabChecklist.translationY = offset
            fabChecklist.alpha = 0f
            fabChecklist.visibility = View.VISIBLE

            fabChecklist.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .start()

            // Reposition fabAdd above checklist if fabAdd is visible
            fabAdd?.let {
                if (it.visibility == View.VISIBLE) {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(parent)
                    constraintSet.clear(R.id.fabAdd, ConstraintSet.BOTTOM)
                    constraintSet.connect(
                        R.id.fabAdd, ConstraintSet.BOTTOM,
                        R.id.fabChecklist, ConstraintSet.TOP, 16
                    )
                    TransitionManager.beginDelayedTransition(parent)
                    constraintSet.applyTo(parent)
                }
            }
        }
    } else {
        fabChecklist.animate()
            .translationY(offset)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                fabChecklist.visibility = View.GONE

                // Reposition fabAdd to bottom if it's still visible
                fabAdd?.let {
                    if (it.visibility == View.VISIBLE) {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(parent)
                        constraintSet.clear(R.id.fabAdd, ConstraintSet.BOTTOM)
                        constraintSet.connect(
                            R.id.fabAdd, ConstraintSet.BOTTOM,
                            ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 32
                        )
                        TransitionManager.beginDelayedTransition(parent)
                        constraintSet.applyTo(parent)
                    }
                }
            }
            .start()
    }
}