package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import java.util.Locale

@Composable
fun ArchiveItem(
    isTablet: Boolean = false, title: String, accessRole: AccessRole?, showSubtitle: Boolean = true
) {
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = if (isTablet) R.drawable.ic_archive_placeholder_multicolor else R.drawable.ic_archive_gradient),
                contentDescription = "",
                modifier = Modifier.size(if (isTablet) 48.dp else 18.dp)
            )
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp, vertical = if (isTablet) 32.dp else 24.dp
                    )
                    .weight(1.0f, fill = true),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    fontSize = if (isTablet) 18.sp else 14.sp,
                    lineHeight = 24.sp,
                    color = Color.White,
                    fontFamily = boldFont
                )
                if (!isTablet && showSubtitle) {
                    Text(
                        text = stringResource(id = R.string.invited_as) + " " + accessRole?.name?.toLowerCase(
                            Locale.getDefault()
                        ),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = regularFont
                    )
                }
            }
            if (isTablet) {
                AccessRoleLabel(accessRole = accessRole)
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
    }
}

@Preview
@Composable
fun ArchiveItemPreview() {
    ArchiveItem(
        isTablet = true, title = "The Flavia Handrea Archive", accessRole = AccessRole.VIEWER
    )
}