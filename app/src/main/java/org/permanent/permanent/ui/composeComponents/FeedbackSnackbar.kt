package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun FeedbackSnackbar(
    title: String,
    subtitle: String,
    isForSuccess: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val blue600Color = Color(ContextCompat.getColor(context, R.color.blue600))
    val success25Color = Color(ContextCompat.getColor(context, R.color.success25))
    val success200Color = Color(ContextCompat.getColor(context, R.color.success200))
    val success500Color = Color(ContextCompat.getColor(context, R.color.success500))
    val error25Color = Color(ContextCompat.getColor(context, R.color.error25))
    val error200Color = Color(ContextCompat.getColor(context, R.color.error200))
    val error500Color = Color(ContextCompat.getColor(context, R.color.error500))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Snackbar(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .border(
                1.dp,
                if (isForSuccess) success200Color else error200Color,
                RoundedCornerShape(10.dp)
            ),
        containerColor = if (isForSuccess) success25Color else error25Color,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(top = 36.dp)
                ) {
                    Image(
                        painter = painterResource(id = if (isForSuccess) R.drawable.ic_check_circle_green else R.drawable.ic_error_square),
                        contentDescription = "Next",
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 34.dp)
                        .weight(1.0f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        color = if (isForSuccess) success500Color else error500Color,
                        fontFamily = regularFont
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = blue600Color,
                        fontFamily = regularFont
                    )
                }
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 16.dp, top = 36.dp, bottom = 34.dp)
                        .clickable { onDismiss() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_close_white),
                        contentDescription = "Next",
                        colorFilter = ColorFilter.tint(if (isForSuccess) success500Color else error500Color),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun FeedbackSnackbarPreview() {
    FeedbackSnackbar(
        title = "Gift code redeemed!",
        subtitle = "4 GB of storage",
        isForSuccess = true,
        onDismiss = { /*TODO*/ })
}
