package org.permanent.permanent.ui.login.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun DowntimeAlert(
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colorResource(R.color.blue900), shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .padding(24.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_warning_orange),
            contentDescription = "",
            colorFilter = ColorFilter.tint(colorResource(R.color.warning500)),
            modifier = Modifier.size(20.dp)
        )

        Text(
            modifier = Modifier.weight(1.0f, fill = false),
            text = buildAnnotatedString {
                append(stringResource(R.string.permanent_down_start) + " ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.permanent_down_middle))
                }
                append(" " + stringResource(R.string.permanent_down_end))
            },
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = colorResource(R.color.blue100),
            fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf))
        )

        Image(
            painter = painterResource(id = R.drawable.ic_close_white),
            contentDescription = "Close",
            colorFilter = ColorFilter.tint(colorResource(R.color.blue400)),
            modifier = Modifier.size(20.dp).clickable { onCloseClick() }
        )
    }
}