package org.permanent.permanent.ui.shareManagement.shareLink

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.recordMenu.compose.RecordMenuHeader
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun ShareLinkScreen(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {

    val recordThumbURL by viewModel.recordThumb.collectAsState()
    val recordName by viewModel.recordName.collectAsState()
    val recordSize by viewModel.recordSize.collectAsState()
    val recordDate by viewModel.recordDate.collectAsState()
    val shareLink by viewModel.shareLink.collectAsState()
    val creatingLink by viewModel.isCreatingLinkState.collectAsState()
    val isLinkSharedState by viewModel.isLinkSharedState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .background(colorResource(R.color.blue25))
            .wrapContentHeight(Alignment.Top)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            SheetHeader(
                title = "Share Item", onCloseClick = onClose
            )

            RecordMenuHeader(
                recordThumbURL = recordThumbURL,
                recordName = recordName,
                recordSize = recordSize,
                recordDate = recordDate
            )

            if (creatingLink) {
                CreatingLinkRow()
            } else {
                if (isLinkSharedState) {
                    SharedLinkRow(shareLink = viewModel.cleanUrlRegex(shareLink), onSettingClick = {}, onCopyClick = {
                        viewModel.copyLinkToClipboard()
                    })
                } else {
                    CreateLinkRow(onClick = {
                        viewModel.onCreateLinkBtnClick()
                    })
                }
            }
        }
    }
}

@Composable
fun CreatingLinkRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .padding(start = 36.dp, top = 24.dp, bottom = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        CircularProgressIndicator(
            overlayColor = OverlayColor.LIGHT, modifier = Modifier
                .height(16.dp)
                .width(16.dp)
        )

        Spacer(modifier = Modifier.width(28.dp))

        Text(
            text = stringResource(R.string.creating_link), style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorResource(R.color.barneyPurple), colorResource(R.color.colorAccent)
                    ), start = Offset(0f, 0f), end = Offset(300f, 300f)
                ),
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SharedLinkRow(
    shareLink: String, onSettingClick: () -> Unit, onCopyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .padding(start = 24.dp, top = 6.dp, bottom = 6.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.blue50),
                    shape = RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(R.color.white))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock_closed),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = shareLink, modifier = Modifier.weight(1f), style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        textAlign = TextAlign.Left,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(R.color.barneyPurple),
                                colorResource(R.color.colorAccent)
                            ), start = Offset(0f, 0f), end = Offset(300f, 300f)
                        ),
                    ), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onSettingClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_blue),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onCopyClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy_blue),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
fun CreateLinkRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .clickable { onClick() }
            .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {

        Icon(
            painter = painterResource(id = R.drawable.ic_share_link),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .padding(top = 4.dp, end = 24.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = stringResource(R.string.create_link_to_sare), style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.create_link_description), style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue400),
                ), maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SheetHeader(
    title: String, onCloseClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.white))
            .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title, style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.blue900),
                textAlign = TextAlign.Center
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )

        if (onCloseClick != null) {
            IconButton(
                onClick = onCloseClick, modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_light_blue),
                    contentDescription = "Close",
                    tint = colorResource(R.color.blue400),
                )
            }
        }
    }
}

enum class LinkState {
    SHARED, LOADING, NOTSHARED
}
