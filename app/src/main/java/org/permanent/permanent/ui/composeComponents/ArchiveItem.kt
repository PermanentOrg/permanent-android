package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole

@Composable
fun ArchiveItem(
    isTablet: Boolean = false,
    isForWelcomePage: Boolean = false,
    iconURL: String? = null,
    title: String,
    accessRole: AccessRole?,
    showSubtitle: Boolean = true,
    showSeparator: Boolean = true,
    showAcceptButton: Boolean = false,
    showAcceptedLabel: Boolean = false,
    onButtonClick: () -> Unit? = {}
) {
    if (isTablet && isForWelcomePage) {
        TabletBodyForWelcomePage(
            iconURL,
            title,
            accessRole,
            showSeparator,
            showAcceptButton,
            showAcceptedLabel,
            onButtonClick
        )
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconURL == null) {
                    Image(
                        painter = painterResource(id = if (isTablet) R.drawable.ic_archive_placeholder_multicolor else R.drawable.ic_archive_gradient),
                        contentDescription = "",
                        modifier = Modifier.size(if (isTablet) 48.dp else 18.dp)
                    )
                } else {
                    AsyncImage(
                        model = iconURL,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = if (isTablet) 32.dp else 24.dp)
                        .weight(1.0f, fill = true),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = title,
                        fontSize = if (isTablet) 18.sp else 14.sp,
                        lineHeight = 24.sp,
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.open_sans_bold_ttf))
                    )
                    if (!isTablet && showSubtitle) {
                        Text(
                            text = (if (isForWelcomePage) stringResource(id = R.string.invited_as) + " " else "") + (accessRole?.toTitleCase()
                                ?: ""),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf))
                        )
                    }
                }
                if (isTablet) {
                    AccessRoleLabel(accessRole = accessRole)
                }
                if (showAcceptButton) {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(40.dp)
                    ) {
                        CenteredTextAndIconButton(
                            buttonColor = ButtonColor.TRANSPARENT,
                            text = stringResource(id = R.string.accept),
                            fontSize = 12.sp,
                            icon = null
                        ) {
                            onButtonClick()
                        }
                    }
                }
                if (showAcceptedLabel) {
                    AcceptedLabel()
                }
            }
            if (showSeparator) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
            }
        }
    }
}

@Composable
private fun TabletBodyForWelcomePage(
    iconURL: String? = null,
    title: String,
    accessRole: AccessRole?,
    showSeparator: Boolean = true,
    showAcceptButton: Boolean = false,
    showAcceptedLabel: Boolean = false,
    onButtonClick: () -> Unit? = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconURL == null) {
                Image(
                    painter = painterResource(id = R.drawable.ic_archive_placeholder_multicolor),
                    contentDescription = "",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                AsyncImage(
                    model = iconURL,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .weight(1.0f, fill = true),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.open_sans_bold_ttf))
                )
                Text(
                    text = stringResource(id = R.string.invited_as) + " " + accessRole?.toTitleCase(),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf))
                )
            }
            if (showAcceptButton) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(48.dp)
                ) {
                    CenteredTextAndIconButton(
                        buttonColor = ButtonColor.TRANSPARENT,
                        text = stringResource(id = R.string.accept),
                        fontSize = 12.sp,
                        icon = null
                    ) {
                        onButtonClick()
                    }
                }
            }
            if (showAcceptedLabel) {
                AcceptedLabel()
            }
        }
        if (showSeparator) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
        }
    }
}

@Preview
@Composable
fun ArchiveItemPreview() {
    ArchiveItem(
        title = "The Flavia Handrea Archive", accessRole = AccessRole.VIEWER
    ) { }
}