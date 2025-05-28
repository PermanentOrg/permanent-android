package org.permanent.permanent.ui.composeComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.permanent.permanent.R

@Composable
fun AnimatedSnackbar(
    modifier: Modifier = Modifier,
    isForError: Boolean = true,
    message: String,
    showButton: Boolean = true,
    buttonText: String = stringResource(id = R.string.ok),
    onButtonClick: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }

    // LaunchedEffect ensures animations happen sequentially when the message changes
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            visible = false  // Start by hiding the old Snackbar
            delay(300)       // Wait for exit animation to complete
            visible = true   // Then show the new Snackbar
        } else visible = false
    }

    AnimatedVisibility(visible = visible,
        enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight } // Slide in from bottom
        ) + fadeIn(), // Fade in as well
        exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight } // Slide out to bottom
        ) + fadeOut(), // Fade out as well
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorResource(id = if (isForError) R.color.errorLight else  R.color.success200), RoundedCornerShape(12.dp))
                .background(
                    color = colorResource(id = if (isForError) R.color.errorSuperLight else R.color.successLight),
                    shape = RoundedCornerShape(size = 12.dp)
                ), contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = if (isForError) R.drawable.ic_error_cercle else R.drawable.ic_check_circle_green),
                    contentDescription = "error",
                    colorFilter = ColorFilter.tint(
                        if (isForError) colorResource(id = R.color.error500) else colorResource(
                            id = R.color.successDark
                        )
                    ),
                    modifier = Modifier.padding(top = 4.dp).size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = message,
                    color = if (isForError) colorResource(id = R.color.error500) else colorResource(
                        id = R.color.blue900
                    ),
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                    fontSize = 14.sp,
                    lineHeight = 24.sp
                )

                if (showButton) {
                    if (isForError) {
                        Box(modifier = Modifier
                            .clickable { onButtonClick() }
                            .padding(start = 8.dp)) {
                            Text(
                                text = buttonText,
                                color = colorResource(id = R.color.error500),
                                fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf)),
                                fontSize = 14.sp,
                                lineHeight = 24.sp
                            )
                        }
                    } else {
                        Box(modifier = Modifier
                            .clickable { onButtonClick() }
                            .padding(start = 8.dp),
                            contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_close_white),
                                contentDescription = "Close",
                                colorFilter = ColorFilter.tint(colorResource(id = R.color.blue200)),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class SnackbarType {
    SUCCESS, ERROR, NONE
}

@Preview
@Composable
fun AnimatedSnackbarPreview() {
    AnimatedSnackbar(
        message = "The entered data is invalid",
        buttonText = "OK",
        onButtonClick = { /*TODO*/ })
}
