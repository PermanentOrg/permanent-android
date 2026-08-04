package org.permanent.permanent.ui.fileView.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.fileView.PreviewOverlayState
import org.permanent.permanent.viewmodels.FileViewViewModel

/**
 * Branded loader/failure/offline overlay for the non-image previews (video/PDF/docs) —
 * the same visual vocabulary as the image viewer (VSP-1754). Renders nothing while the
 * preview is healthy; otherwise it covers the preview with the blurred record thumbnail
 * (or the skeleton when there is none) and either the translucent spinner (loading) or
 * the S6/S7 card, cross-fading between them in place.
 *
 * Also hosts the download/publish busy spinner, drawn above everything else so it stays
 * visible over the failure card too.
 */
@Composable
fun PreviewStatusOverlay(viewModel: FileViewViewModel, thumbnailUrl: String?) {
    val state by viewModel.previewState.collectAsState()
    val isBusy by viewModel.isBusy.observeAsState(false)
    // Hoisted above the state check so the bitmap survives NONE and each state
    // transition reuses it instead of cancelling and re-issuing the request
    val thumbnail by rememberThumbnailBitmap(thumbnailUrl, onFailed = {})

    if (state != PreviewOverlayState.NONE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val bitmap = thumbnail
            if (bitmap != null) {
                BlurredThumbnailBackdrop(
                    bitmap = bitmap,
                    blurRadiusPx = THUMB_BLUR_RADIUS_PX,
                    scrimAlpha = SCRIM_ALPHA,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                PreviewSkeletonBackground(modifier = Modifier.align(Alignment.Center))
            }

            ImageStatusOverlay(
                kind = when (state) {
                    // Screen blending vanishes on the light skeleton, so it gets the
                    // plain-white spinner — same split the image viewer makes (S2 vs S5)
                    PreviewOverlayState.LOADING ->
                        if (bitmap != null) ImageOverlayKind.LOADER
                        else ImageOverlayKind.LOADER_ON_SKELETON

                    PreviewOverlayState.FAILED -> ImageOverlayKind.LOAD_FAILED
                    else -> ImageOverlayKind.OFFLINE
                },
                onCardTap = viewModel::onPreviewCardTapped,
                isImageContent = false,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // Download/publish in flight — the app-wide scrimmed spinner, composed only while
    // busy so its infinite animation isn't ticking for the life of the page
    if (isBusy == true) {
        CircularProgressIndicator()
    }
}
