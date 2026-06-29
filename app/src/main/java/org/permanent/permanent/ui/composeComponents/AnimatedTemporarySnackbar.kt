package org.permanent.permanent.ui.composeComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.permanent.permanent.R

@Composable
fun AnimatedTemporarySnackbar(
    modifier: Modifier = Modifier,
    type: TemporarySnackbarType = TemporarySnackbarType.ERROR,
    message: String,
    onButtonClick: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            visible = false          // hide previous snackbar
            delay(300)    // wait for exit animation
            visible = true           // show new snackbar
            delay(4_000)  // 👈 show duration (4 seconds)
            onButtonClick()          // hide automatically
        } else visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight } // Slide in from bottom
        ) + fadeIn(), // Fade in as well
        exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight } // Slide out to bottom
        ) + fadeOut(), // Fade out as well
        modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.darkGrey),
                    shape = RoundedCornerShape(size = 12.dp)
                ), contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.Top, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = if (type == TemporarySnackbarType.ERROR) R.drawable.ic_error_circle_full else if (type == TemporarySnackbarType.WARNING) R.drawable.ic_warning_orange else R.drawable.ic_check_circle_green),
                    contentDescription = "error",
                    colorFilter = ColorFilter.tint(
                        colorResource(
                            id = if (type == TemporarySnackbarType.ERROR) R.color.error400 else if (type == TemporarySnackbarType.WARNING) R.color.warning400 else R.color.success500
                        )
                    ),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = message,
                    color = colorResource(
                        id = if (type == TemporarySnackbarType.ERROR) R.color.error400 else if (type == TemporarySnackbarType.WARNING) R.color.warning400 else R.color.white
                    ),
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    fontSize = 14.sp,
                    lineHeight = 24.sp
                )

                Box(modifier = Modifier
                    .clickable { onButtonClick() }
                    .padding(start = 8.dp, top = 4.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_close_white),
                        contentDescription = "Close",
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.whiteSuperTransparent)),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

enum class TemporarySnackbarType {
    SUCCESS, ERROR, WARNING, NONE
}

@Preview
@Composable
fun SnackbarPreview() {
    AnimatedTemporarySnackbar(
        message = "The entered data is invalid", onButtonClick = { /*TODO*/ })
}