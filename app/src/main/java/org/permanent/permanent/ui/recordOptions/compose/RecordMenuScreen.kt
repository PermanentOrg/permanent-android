package org.permanent.permanent.ui.recordOptions.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.SettingsMenuItem
import org.permanent.permanent.viewmodels.RecordMenuItem
import org.permanent.permanent.viewmodels.RecordMenuViewModel

@Composable
fun RecordMenuScreen(
    viewModel: RecordMenuViewModel,
    onItemClick: (RecordMenuItem) -> Unit,
    onClose: () -> Unit,
) {
    val isBusyState by viewModel.isBusyState.collectAsState()
    val recordThumbURL by viewModel.recordThumb.collectAsState()
    val recordName by viewModel.recordName.collectAsState()
    val recordSize by viewModel.recordSize.collectAsState()
    val recordDate by viewModel.recordDate.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        RecordMenuHeader(
            recordThumbURL = recordThumbURL,
            recordName = recordName,
            recordSize = recordSize,
            recordDate = recordDate,
            onCloseClick = onClose
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            when {
                isBusyState -> {
                    CircularProgressIndicator(overlayColor = OverlayColor.LIGHT)
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .background(Color.White)
                    ) {
                        menuItems.forEach { item ->
                            when (item) {
                                RecordMenuItem.Share -> SettingsMenuItem(
                                    iconResource = painterResource(id = R.drawable.ic_share_primary),
                                    text = stringResource(R.string.share_and_manage_access),
                                ) { onItemClick(item) }

                                RecordMenuItem.Publish -> SettingsMenuItem(
                                    iconResource = painterResource(id = R.drawable.ic_publish_primary),
                                    text = stringResource(R.string.publish_on_the_web),
                                ) { onItemClick(item) }

                                RecordMenuItem.SendACopy -> SettingsMenuItem(
                                    iconResource = painterResource(id = R.drawable.ic_send_primary),
                                    text = stringResource(R.string.send_a_copy),
                                ) { onItemClick(item) }

                                RecordMenuItem.Download -> SettingsMenuItem(
                                    iconResource = painterResource(id = R.drawable.ic_download_primary),
                                    text = stringResource(R.string.download),
                                ) { onItemClick(item) }

                                RecordMenuItem.Rename -> SettingsMenuItem(
                                    iconResource = painterResource(id = R.drawable.ic_rename_primary),
                                    text = stringResource(R.string.rename),
                                ) { onItemClick(item) }

                                RecordMenuItem.Move -> SettingsMenuItem(
                                    iconResource = painterResource(id = R.drawable.ic_move_primary),
                                    text = stringResource(R.string.move_to_another_folder),
                                ) { onItemClick(item) }

                                RecordMenuItem.Copy -> SettingsMenuItem(
                                    iconResource = painterResource(id = R.drawable.ic_copy_primary),
                                    text = stringResource(R.string.copy_to_another_folder),
                                ) { onItemClick(item) }

                                RecordMenuItem.Delete -> {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        color = colorResource(R.color.blue50)
                                    )
                                    SettingsMenuItem(
                                        iconResource = painterResource(id = R.drawable.ic_delete_large_red),
                                        text = stringResource(R.string.delete),
                                        itemColor = colorResource(R.color.error500),
                                    ) { onItemClick(item) }
                                }

                                RecordMenuItem.LeaveShare -> {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        color = colorResource(R.color.blue50)
                                    )
                                    SettingsMenuItem(
                                        iconResource = painterResource(id = R.drawable.ic_leave_share_red),
                                        text = stringResource(R.string.leave_share),
                                        itemColor = colorResource(R.color.error500),
                                    ) { onItemClick(item) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordMenuHeader(
    recordThumbURL: String,
    recordName: String,
    recordSize: String,
    recordDate: String,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        if (recordThumbURL.isNotEmpty()) {
            AsyncImage(
                model = recordThumbURL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder_purple_gradient),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Texts
        Column(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .padding(top = 4.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = recordName, style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val infoText = listOf(recordSize, recordDate)
                .filter { it.isNotBlank() }
                .joinToString(" • ")

            Text(
                text = infoText,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue400),
                )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Close Icon
        IconButton(onClick = onCloseClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_light_blue),
                contentDescription = "Close",
                tint = colorResource(R.color.blue400),
            )
        }
    }
}