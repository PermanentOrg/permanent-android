package org.permanent.permanent.ui.fileView.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import org.permanent.permanent.ui.fileView.PreviewErrorState
import org.permanent.permanent.viewmodels.FileViewViewModel

/**
 * Branded failure/offline card for the non-image previews (video/PDF/docs) — replaces
 * the generic error label + retry button. Renders nothing while the preview is healthy;
 * on failure it covers the preview with the blurred record thumbnail (or the skeleton
 * when there is none) and the same card the image viewer shows for S6/S7.
 */
@Composable
fun PreviewErrorOverlay(viewModel: FileViewViewModel, thumbnailUrl: String?) {
    val state by viewModel.previewErrorState.collectAsState()
    if (state == PreviewErrorState.NONE) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val thumbnail by rememberThumbnailBitmap(thumbnailUrl, onFailed = {})
        val bitmap = thumbnail
        if (bitmap != null) {
            // Aspect-fitted like the preview beneath, so letterbox areas stay black
            val blurModifier = if (isLiveBlurSupported) {
                Modifier.blur(with(LocalDensity.current) { THUMB_BLUR_RADIUS_PX.toDp() })
            } else {
                Modifier // bitmap is pre-blurred below API 31
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
                        .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                )
            }
        } else {
            PreviewSkeletonBackground(modifier = Modifier.align(Alignment.Center))
        }

        ImageStatusOverlay(
            kind = if (state == PreviewErrorState.FAILED) ImageOverlayKind.LOAD_FAILED
            else ImageOverlayKind.OFFLINE,
            onCardTap = viewModel::onPreviewCardTapped,
            isImageContent = false,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
