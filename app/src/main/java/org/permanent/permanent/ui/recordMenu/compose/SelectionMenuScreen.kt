package org.permanent.permanent.ui.recordMenu.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import org.permanent.permanent.ui.composeComponents.SettingsMenuItem
import org.permanent.permanent.viewmodels.RecordMenuItem
import org.permanent.permanent.viewmodels.SelectionMenuViewModel

@Composable
fun SelectionMenuScreen(
    viewModel: SelectionMenuViewModel,
    onItemClick: (RecordMenuItem) -> Unit,
    onClose: () -> Unit,
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val headerInfo by viewModel.headerInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
    ) {
        headerInfo?.let { info ->
            RecordsMenuHeader(
                title = info.name,
                infoText = info.infoText,
                thumbUrl = info.thumbUrl ?: "",
                showMultipleShadow = info.showMultipleShadow,
                onCloseClick = onClose
            )
        }

        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
        ) {
            menuItems.forEach { item ->
                when (item) {
                    RecordMenuItem.EditMetadata -> SettingsMenuItem(
                        iconResource = painterResource(id = R.drawable.ic_edit_small_primary),
                        text = stringResource(R.string.edit_files_metadata),
                    ) { onItemClick(item) }

                    RecordMenuItem.Copy -> SettingsMenuItem(
                        iconResource = painterResource(id = R.drawable.ic_copy_primary),
                        text = stringResource(R.string.copy_to_another_folder),
                    ) { onItemClick(item) }

                    RecordMenuItem.Move -> SettingsMenuItem(
                        iconResource = painterResource(id = R.drawable.ic_move_primary),
                        text = stringResource(R.string.move_to_another_folder),
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

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun RecordsMenuHeader(
    title: String,
    infoText: String,
    thumbUrl: String,
    showMultipleShadow: Boolean,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(40.dp)) {
                if (thumbUrl.isNotEmpty()) {
                    AsyncImage(
                        model = thumbUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_folder_purple_gradient),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            if (showMultipleShadow) {
                val offsetY = if (thumbUrl.isEmpty()) (-2).dp else 0.dp

                Image(
                    painter = painterResource(id = R.drawable.ic_multiple_shadow),
                    contentDescription = null,
                    modifier = Modifier.offset(y = offsetY))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = infoText,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue400)
                )
            )
        }

        IconButton(onClick = onCloseClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_light_blue),
                contentDescription = "Close",
                tint = colorResource(R.color.blue400),
            )
        }
    }
}
