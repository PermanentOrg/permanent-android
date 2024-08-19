package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.permanent.permanent.R

@Composable
fun AcceptedLabel(
) {
    Row(
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = stringResource(id = R.string.accepted),
            color = colorResource(id = R.color.success),
            fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf)),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.width(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_done_white),
            contentDescription = stringResource(id = R.string.accepted),
            colorFilter = ColorFilter.tint(colorResource(id = R.color.success)),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview
@Composable
fun AcceptedLabelPreview() {
    AcceptedLabel()
}