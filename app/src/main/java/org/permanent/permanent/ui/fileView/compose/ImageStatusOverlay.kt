package org.permanent.permanent.ui.fileView.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.SpinnerStyle

// Allocated once — ImageErrorCard recomposes every frame of the press animation
private val UsualRegularFontFamily = FontFamily(Font(R.font.usual_regular))

/** What the center overlay of a file preview shows (Figma S2/S5–S8, VSP-1754). */
enum class ImageOverlayKind {
    NONE,

    /** Permanent loader, screen-blended over the blurred photo (S2/S8). */
    LOADER,

    /**
     * Loader over the light S5 skeleton: screen blending vanishes on a light background,
     * so the rings are tinted plain white instead (per the Figma S5 frame).
     */
    LOADER_ON_SKELETON,

    /** S6 — load failed while connected; tap-to-retry card. */
    LOAD_FAILED,

    /** S7 — offline card; auto-retries on reconnect, tap is acknowledged but inert. */
    OFFLINE
}

/**
 * Single component hosting the loader / failure / offline content of the previews
 * (Android counterpart of iOS ImagePreviewStateOverlayView). The loader "transforms in
 * place" into the error card via an in-place cross-fade using the same duration/easing
 * as the S3 dissolve, so the screen keeps one animation vocabulary.
 */
@Composable
fun ImageStatusOverlay(
    kind: ImageOverlayKind,
    onCardTap: () -> Unit,
    modifier: Modifier = Modifier,
    dissolveMillis: Int = CROSS_DISSOLVE_MILLIS,
    isImageContent: Boolean = true
) {
    AnimatedContent(
        targetState = kind,
        transitionSpec = {
            (fadeIn(tween(dissolveMillis, easing = EaseOut)) togetherWith
                    fadeOut(tween(dissolveMillis, easing = EaseOut)))
                .using(SizeTransform(clip = false))
        },
        contentAlignment = Alignment.Center,
        label = "imageStatusOverlay",
        modifier = modifier
    ) { target ->
        when (target) {
            ImageOverlayKind.NONE -> {}

            ImageOverlayKind.LOADER -> CircularProgressIndicator(
                overlayColor = OverlayColor.NONE,
                style = SpinnerStyle.SCREEN_BLENDED,
                modifier = Modifier.size(48.dp)
            )

            // Figma's S5 spinner is white at 50% group opacity (Circles/Spinner-Two-Circles)
            ImageOverlayKind.LOADER_ON_SKELETON -> CircularProgressIndicator(
                overlayColor = OverlayColor.NONE,
                style = SpinnerStyle.WHITE,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer { alpha = 0.5f }
            )

            ImageOverlayKind.LOAD_FAILED -> ImageErrorCard(
                iconRes = R.drawable.ic_retry_arrow_white,
                line1 = stringResource(
                    if (isImageContent) R.string.preview_couldnt_load_image
                    else R.string.preview_couldnt_load_file
                ),
                line2 = stringResource(R.string.preview_tap_to_retry),
                onTap = onCardTap
            )

            ImageOverlayKind.OFFLINE -> ImageErrorCard(
                iconRes = R.drawable.ic_wifi_exclamation_white,
                line1 = stringResource(R.string.preview_youre_offline),
                line2 = stringResource(R.string.preview_connect_full_quality),
                onTap = onCardTap
            )
        }
    }
}

// Per the Figma S5 frame the skeleton is a full-width 390x480 box (the area the photo
// would occupy), letterboxed in black — not a full-screen fill
private const val SKELETON_ASPECT_RATIO = 390f / 480f

/**
 * S5 skeleton background, shown when there is no thumbnail to blur. The asset bakes the
 * full Figma stack (white box + brand mark + backdrop-blur 20 + 16% scrim) because
 * Modifier.blur() is a silent no-op below API 31. Callers center it on the black
 * viewer background.
 */
@Composable
fun PreviewSkeletonBackground(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.img_skeleton_bg),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(SKELETON_ASPECT_RATIO)
    )
}

/**
 * The S6/S7 message card per Figma: black 32% rounded card, white icon and two-line
 * caption. The press acknowledges the tap even when the retry can't proceed (e.g. still
 * offline) — quick scale/alpha dip, iOS parity.
 */
@Composable
private fun ImageErrorCard(
    iconRes: Int,
    line1: String,
    line2: String,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "cardPressScale"
    )
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 1f,
        animationSpec = tween(120),
        label = "cardPressAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                alpha = pressAlpha
            }
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(
                interactionSource = interactionSource, indication = null
            ) { onTap() }
            .padding(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null
            )
        }
        Text(
            text = "$line1\n$line2",
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontFamily = UsualRegularFontFamily,
            textAlign = TextAlign.Center
        )
    }
}
