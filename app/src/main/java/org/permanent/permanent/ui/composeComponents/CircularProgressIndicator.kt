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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import org.permanent.permanent.R

@Composable
fun CircularProgressIndicator(overlayColor: OverlayColor = OverlayColor.DARK, modifier: Modifier = Modifier.fillMaxSize()) {
    Box(
        modifier = modifier
            .background(if (overlayColor == OverlayColor.DARK) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f))
            .clickable(enabled = false) {}, contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "")

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = ""
        )

        Image(painter = painterResource(id = R.drawable.ellipse_exterior),
            contentDescription = null,
            modifier = Modifier.graphicsLayer { rotationZ = -rotation })

        Image(painter = painterResource(id = R.drawable.ellipse_interior),
            contentDescription = null,
            modifier = Modifier.graphicsLayer { rotationZ = rotation })
    }
}

enum class OverlayColor {
    LIGHT,
    DARK
}