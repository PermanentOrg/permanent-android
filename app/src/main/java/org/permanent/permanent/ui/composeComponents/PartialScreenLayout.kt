package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp

@Composable
fun PartialScreenLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.clip(shape = RoundedCornerShape(8.dp)),
        content = content
    ) { measurables, constraints ->
        val placeable = measurables[0].measure(constraints)

        val width = constraints.maxWidth
        val height = (constraints.maxHeight * 0.96).toInt()

        layout(width, height) {
            placeable.placeRelative(
                (constraints.maxWidth - width) / 2,
                0
            )
        }
    }
}