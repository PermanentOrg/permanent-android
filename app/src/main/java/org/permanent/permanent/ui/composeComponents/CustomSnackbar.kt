package org.permanent.permanent.ui.composeComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun CustomSnackbar(
    modifier: Modifier = Modifier,
    isForError: Boolean = true,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
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

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight } // Slide in from bottom
        ) + fadeIn(), // Fade in as well
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight } // Slide out to bottom
        ) + fadeOut(), // Fade out as well
        modifier = modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = if (isForError) R.color.errorLight else R.color.successLight),
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .padding(24.dp), contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = if (isForError) R.drawable.ic_error_cercle else R.drawable.ic_done_white),
                    colorFilter = ColorFilter.tint(
                        if (isForError) colorResource(id = R.color.error500) else colorResource(
                            id = R.color.successDark
                        )
                    ),
                    contentDescription = "error",
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = message,
                    color = if (isForError) colorResource(id = R.color.error500) else colorResource(
                        id = R.color.successDark
                    ),
                    modifier = Modifier.weight(1f),
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                    fontSize = 14.sp,
                    lineHeight = 24.sp
                )

                TextButton(onClick = onButtonClick) {
                    Text(
                        text = buttonText,
                        color = if (isForError) colorResource(id = R.color.blue900) else colorResource(
                            id = R.color.successDark
                        ),
                        fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf)),
                        fontSize = 14.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CustomSnackbarPreview() {
    CustomSnackbar(
        message = "The entered data is invalid",
        buttonText = "OK",
        onButtonClick = { /*TODO*/ })
}
