package org.permanent.permanent.ui.myFiles.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
private fun AddOptionsMenuItem(
    icon: Painter,
    text: String,
    bold: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF131B4A)
        )
    }
}

@Composable
fun AddOptionsScreen(
    onNewFolder: () -> Unit,
    onTakePhoto: () -> Unit,
    onTakeVideo: () -> Unit,
    onUploadPhotos: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(bottom = 8.dp)
        ) {
            // Create New Folder — highlighted with blue25 background + rounded top corners
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colorResource(R.color.blue25),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                AddOptionsMenuItem(
                    icon = painterResource(R.drawable.ic_add_options_create_folder),
                    text = stringResource(R.string.new_folder_title),
                    bold = true,
                    onClick = onNewFolder
                )
            }

            AddOptionsMenuItem(
                icon = painterResource(R.drawable.ic_add_options_take_photo_video),
                text = stringResource(R.string.add_options_menu_item_take_photo),
                onClick = onTakePhoto
            )

            AddOptionsMenuItem(
                icon = painterResource(R.drawable.ic_add_options_take_video),
                text = stringResource(R.string.add_options_menu_item_take_video),
                onClick = onTakeVideo
            )

            AddOptionsMenuItem(
                icon = painterResource(R.drawable.ic_add_options_upload_images),
                text = stringResource(R.string.add_options_upload_photos_from_library),
                onClick = onUploadPhotos
            )
        }
    }
}
