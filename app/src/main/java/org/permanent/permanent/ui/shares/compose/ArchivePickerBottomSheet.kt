@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.shares.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive

@Composable
fun ArchivePickerBottomSheet(
    archives: List<Archive>,
    selectedArchive: Archive?,
    onArchiveSelected: (Archive) -> Unit,
    onCreateArchiveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val mediumFont = FontFamily(Font(R.font.usual_medium))
    val regularFont = FontFamily(Font(R.font.usual_regular))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = colorResource(R.color.white),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_archives_blue),
                    contentDescription = null,
                    tint = colorResource(R.color.blue900),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.share_preview_select_archive),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = mediumFont,
                color = colorResource(R.color.blue900),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_middle_grey),
                    contentDescription = stringResource(R.string.menu_toolbar_close),
                    tint = colorResource(R.color.blue200),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = colorResource(R.color.blue50)
        )

        CreateNewArchiveRow(
            isHighlighted = selectedArchive == null,
            onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismiss()
                        onCreateArchiveClick()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (archives.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.share_preview_no_archives),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = regularFont,
                    color = colorResource(R.color.blue600),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                items(archives, key = { it.id }) { archive ->
                    ArchiveRow(
                        archive = archive,
                        isSelected = archive.id == selectedArchive?.id,
                        onClick = {
                            onArchiveSelected(archive)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateNewArchiveRow(
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val mediumFont = FontFamily(Font(R.font.usual_medium))

    val rowBackground = if (isHighlighted) {
        colorResource(R.color.blue25)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colorResource(R.color.blue900)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus_white),
                contentDescription = null,
                tint = colorResource(R.color.white),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = stringResource(R.string.share_preview_create_new_archive_row),
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontFamily = mediumFont,
            color = colorResource(R.color.blue900),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ArchiveRow(
    archive: Archive,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val regularFont = FontFamily(Font(R.font.usual_regular))
    val mediumFont = FontFamily(Font(R.font.usual_medium))

    val rowBackground = if (isSelected) {
        colorResource(R.color.blue25)
    } else {
        Color.Transparent
    }

    val archiveLabel = formatArchiveName(
        fullName = archive.fullName.orEmpty(),
        regularFont = regularFont,
        mediumFont = mediumFont
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArchiveThumbnail(
            archive = archive,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = archiveLabel,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = colorResource(R.color.blue900),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
