package org.permanent.permanent.ui.composeComponents

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.permanent.permanent.R

@Composable
fun CircularProgressIndicator(
    overlayColor: OverlayColor = OverlayColor.DARK,
    style: SpinnerStyle = SpinnerStyle.GRADIENT,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val backgroundColor = when (overlayColor) {
        OverlayColor.DARK -> Color.Black.copy(alpha = 0.5f)
        OverlayColor.LIGHT -> Color.White.copy(alpha = 0.5f)
        OverlayColor.NONE -> Color.Transparent
    }
    val blendModifier = if (style == SpinnerStyle.SCREEN_BLENDED) {
        Modifier.drawWithContent {
            val paint = Paint().apply { blendMode = BlendMode.Screen }
            drawContext.canvas.saveLayer(Rect(Offset.Zero, size), paint)
            drawContent()
            drawContext.canvas.restore()
        }
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(enabled = false) {}, contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "")

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = ""
        )

        Box(modifier = blendModifier, contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = R.drawable.ellipse_exterior),
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = -rotation })

            Image(painter = painterResource(id = R.drawable.ellipse_interior),
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = rotation })
        }
    }
}

enum class OverlayColor {
    LIGHT,
    DARK,
    NONE
}

enum class SpinnerStyle {
    GRADIENT,

    /** The gradient spinner screen-blended onto the content beneath */
    SCREEN_BLENDED
}

@Preview(showBackground = true, backgroundColor = 0xFF444444)
@Composable
private fun ScreenBlendedSpinnerPreview() {
    CircularProgressIndicator(
        overlayColor = OverlayColor.NONE,
        style = SpinnerStyle.SCREEN_BLENDED,
        modifier = Modifier.size(48.dp)
    )
}
