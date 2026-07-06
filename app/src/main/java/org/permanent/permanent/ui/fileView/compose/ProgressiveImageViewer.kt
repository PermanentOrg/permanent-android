package org.permanent.permanent.ui.fileView.compose

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.SpinnerStyle
import org.permanent.permanent.ui.fileView.ImageViewUiState
import org.permanent.permanent.viewmodels.FileViewViewModel

private const val SCRIM_ALPHA = 0.16f
private const val THUMB_BLUR_RADIUS_PX = 64f
private const val CROSS_DISSOLVE_MILLIS = 200

// Manual-test hook (debug builds only): set to e.g. 2000 to slow the S3 cross-dissolve
// down enough to observe the fade-in / blur-to-0 / loader fade-out
private const val DEBUG_CROSS_DISSOLVE_MILLIS = 0

private val dissolveMillis: Int
    get() = if (BuildConfig.DEBUG && DEBUG_CROSS_DISSOLVE_MILLIS > 0) {
        DEBUG_CROSS_DISSOLVE_MILLIS
    } else {
        CROSS_DISSOLVE_MILLIS
    }

// Modifier.blur() is a silent no-op below API 31 — those devices get a pre-blurred bitmap
private val isLiveBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

// Bitmap-level blur for API < 31, where Modifier.blur() is a no-op. Applied to the 256 px
// thumbnail before upscaling, so the radius is scaled down (~64 px at screen scale).
private const val LEGACY_BLUR_RADIUS = 13
private const val LEGACY_BLUR_SAMPLING = 1

/**
 * Full-screen image viewer with progressive Blur -> Sharp loading (VSP-1754):
 * S1 blurred thumbnail + scrim, S2 white loader after 500 ms, S3 200 ms cross-dissolve,
 * S4 sharp full-resolution image with pinch/pan/double-tap zoom.
 */
@Composable
fun ProgressiveImageViewer(viewModel: FileViewViewModel, onTap: () -> Unit) {
    val uiState by viewModel.imageViewUiState.collectAsState()
    val thumbnailUrl by viewModel.thumbnailUrl.collectAsState()
    val fullResSource by viewModel.fullResImageSource.collectAsState()
    val context = LocalContext.current

    val isSharp = uiState is ImageViewUiState.Sharp
    val dissolveSpec = remember { tween<Float>(durationMillis = dissolveMillis, easing = EaseOut) }
    val fullResAlpha by animateFloatAsState(
        targetValue = if (isSharp) 1f else 0f, animationSpec = dissolveSpec, label = "fullResAlpha"
    )
    val blurRadiusPx by animateFloatAsState(
        targetValue = if (isSharp) 0f else THUMB_BLUR_RADIUS_PX,
        animationSpec = dissolveSpec,
        label = "blurRadius"
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isSharp) 0f else SCRIM_ALPHA, animationSpec = dissolveSpec, label = "scrimAlpha"
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) { onTap() }) {

        // Blurred thumbnail + scrim (S1/S2); fully covered by the full-res image once
        // the cross-dissolve completes, so it can be dropped from composition.
        // The thumbnail keeps the original's aspect ratio, so this box matches the
        // bounds the sharp image (ContentScale.Fit) ends up in — no size jump in S3.
        if (fullResAlpha < 1f) {
            val thumbnail by rememberThumbnailBitmap(thumbnailUrl)
            thumbnail?.let { bitmap ->
                val blurModifier = if (isLiveBlurSupported) {
                    Modifier.blur(with(LocalDensity.current) { blurRadiusPx.toDp() })
                } else {
                    Modifier // bitmap is pre-blurred below API 31; S3 degrades to a cross-fade
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(blurModifier)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = scrimAlpha))
                    )
                }
            }
        }

        // Full-resolution image (S3/S4) — composing it starts the background download
        fullResSource?.let { source ->
            ZoomableAsyncImage(
                model = remember(source) {
                    ImageRequest.Builder(context)
                        .data(source)
                        .listener(
                            onSuccess = { _, _ -> viewModel.onFullResReady() },
                            // STUB: companion error-handling ticket
                            onError = { _, _ -> viewModel.onFullResFailed() })
                        .build()
                },
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alpha = fullResAlpha,
                onClick = { onTap() },
                modifier = Modifier.fillMaxSize()
            )
        }

        // White loader (S2), fading out during the cross-dissolve
        AnimatedVisibility(
            visible = uiState is ImageViewUiState.BlurredThumbnailWithLoader,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(dissolveMillis, easing = EaseOut)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(
                overlayColor = OverlayColor.NONE,
                style = SpinnerStyle.SCREEN_BLENDED,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Loads the thumbnail through Picasso so it hits the same cache the file list already
 * populated (instant, even offline). Below API 31 the bitmap is pre-blurred, since
 * Modifier.blur() is a silent no-op there.
 */
@Composable
private fun rememberThumbnailBitmap(url: String?): androidx.compose.runtime.State<Bitmap?> {
    val context = LocalContext.current
    val bitmapState = remember(url) { mutableStateOf<Bitmap?>(null) }
    // Picasso holds targets weakly — remember keeps a strong reference while composed
    val target = remember(url) {
        object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmapState.value = bitmap
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
    }
    DisposableEffect(url) {
        if (!url.isNullOrEmpty()) {
            var request = Picasso.get().load(url)
            if (!isLiveBlurSupported) {
                request = request.transform(
                    BlurTransformation(context, LEGACY_BLUR_RADIUS, LEGACY_BLUR_SAMPLING)
                )
            }
            request.into(target)
        }
        onDispose { Picasso.get().cancelRequest(target) }
    }
    return bitmapState
}
