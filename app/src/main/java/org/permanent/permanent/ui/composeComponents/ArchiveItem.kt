package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun ArchiveItem(
    isTablet: Boolean = false, title: String, subtitle: String, showSubtitle: Boolean = true
) {
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_archive_gradient),
                contentDescription = "",
                modifier = Modifier.size(18.dp)
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .weight(1.0f, fill = false),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    fontSize = if (isTablet) 18.sp else 14.sp,
                    lineHeight = 24.sp,
                    color = Color.White,
                    fontFamily = boldFont
                )
                if (showSubtitle) {
                    Text(
                        text = subtitle,
                        fontSize = if (isTablet) 18.sp else 12.sp,
                        lineHeight = if (isTablet) 32.sp else 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = regularFont
                    )
                }
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
    }
}

@Preview
@Composable
fun ArchiveItemPreview() {
    ArchiveItem(
        title = "The Flavia Handrea Archive", subtitle = "Invited as viewer"
    )
}