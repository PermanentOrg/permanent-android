package org.permanent.permanent.ui.fileView.compose

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.request.ImageRequest
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.ui.fileView.ImageViewUiState
import org.permanent.permanent.viewmodels.FileViewViewModel

internal const val SCRIM_ALPHA = 0.16f
internal const val THUMB_BLUR_RADIUS_PX = 64f
internal const val CROSS_DISSOLVE_MILLIS = 200

// Manual-test hook (debug builds only): set to e.g. 2000 to slow the S3 cross-dissolve
// down enough to observe the fade-in / blur-to-0 / loader fade-out
private const val DEBUG_CROSS_DISSOLVE_MILLIS = 0

private val dissolveMillis: Int
    get() = if (BuildConfig.DEBUG && DEBUG_CROSS_DISSOLVE_MILLIS > 0) {
        DEBUG_CROSS_DISSOLVE_MILLIS
    } else {
        CROSS_DISSOLVE_MILLIS
    }

// Manual-test hook (debug builds only): set to true to use pre-blurred bitmaps on ALL
// API levels — isolates whether pager flicker artifacts come from the RenderEffect blur
private const val DEBUG_FORCE_LEGACY_BLUR = false

// Modifier.blur() is a silent no-op below API 31 — those devices get a pre-blurred bitmap
internal val isLiveBlurSupported: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !(BuildConfig.DEBUG && DEBUG_FORCE_LEGACY_BLUR)

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
    val isThumbnailFailed by viewModel.isThumbnailFailed.collectAsState()
    val fullResSource by viewModel.fullResImageSource.collectAsState()
    val retryNonce by viewModel.fullResRetryNonce.collectAsState()
    val context = LocalContext.current

    // S5 — no thumbnail to blur: the skeleton replaces it as the background layer
    val showSkeleton = thumbnailUrl == null || isThumbnailFailed

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
            if (showSkeleton) {
                // S5 skeleton in place of the blurred thumbnail; no scrim — the
                // loader/card reads directly on the light fill
                PreviewSkeletonBackground(modifier = Modifier.align(Alignment.Center))
            }
            val thumbnail by rememberThumbnailBitmap(thumbnailUrl, viewModel::onThumbnailFailed)
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
                // The nonce makes the retried request non-equal so Coil restarts it (S8);
                // it's excluded from the cache key, so successes still hit the memory cache
                model = remember(source, retryNonce) {
                    ImageRequest.Builder(context)
                        .data(source)
                        .setParameter("retryNonce", retryNonce, memoryCacheKey = null)
                        .listener(
                            onSuccess = { _, _ -> viewModel.onFullResReady() },
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

        // Center overlay: loader (S2/S8) or failure/offline card (S6/S7); the loader
        // cross-fades in place into the card and fades out during the S3 dissolve
        ImageStatusOverlay(
            kind = when (uiState) {
                ImageViewUiState.BlurredThumbnailWithLoader ->
                    if (showSkeleton) ImageOverlayKind.LOADER_ON_SKELETON
                    else ImageOverlayKind.LOADER

                ImageViewUiState.LoadFailed -> ImageOverlayKind.LOAD_FAILED
                ImageViewUiState.Offline -> ImageOverlayKind.OFFLINE
                else -> ImageOverlayKind.NONE
            },
            onCardTap = viewModel::onErrorCardTapped,
            dissolveMillis = dissolveMillis,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Loads the thumbnail through Picasso so it hits the same cache the file list already
 * populated (instant, even offline). Below API 31 the bitmap is pre-blurred, since
 * Modifier.blur() is a silent no-op there. A failed request is reported through
 * [onFailed] so the viewer can fall back to the S5 skeleton.
 */
@Composable
internal fun rememberThumbnailBitmap(
    url: String?,
    onFailed: () -> Unit
): androidx.compose.runtime.State<Bitmap?> {
    val context = LocalContext.current
    val bitmapState = remember(url) { mutableStateOf<Bitmap?>(null) }
    // rememberUpdatedState keeps the remember(url) target below stable across recompositions
    val currentOnFailed by rememberUpdatedState(onFailed)
    // Picasso holds targets weakly — remember keeps a strong reference while composed
    val target = remember(url) {
        object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmapState.value = bitmap
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                currentOnFailed()
            }

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
