package org.permanent.permanent.ui.shares.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.shareManagement.compose.AccessType
import org.permanent.permanent.ui.shares.PreviewState
import org.permanent.permanent.viewmodels.SharePreviewViewModel

@Composable
fun SharePreviewScreen(
    viewModel: SharePreviewViewModel
) {
    val archiveThumbURL by viewModel.archiveThumbURL.collectAsState()
    val rawAccountName by viewModel.rawAccountName.collectAsState()
    val rawArchiveName by viewModel.rawArchiveName.collectAsState()
    val currentState by viewModel.currentState.collectAsState()
    val records by viewModel.records.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()
    val accessType by viewModel.accessType.collectAsState()
    val archives by viewModel.archives.collectAsState()
    val selectedArchive by viewModel.selectedArchive.collectAsState()
    var showArchivePickerSheet by remember { mutableStateOf(false) }

    val boldFont = FontFamily(Font(R.font.usual_bold))
    val mediumFont = FontFamily(Font(R.font.usual_medium))
    val regularFont = FontFamily(Font(R.font.usual_regular))

    val hasBlurredContent = accessType != AccessType.ANYONE_CAN_VIEW

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.blue25))
    ) {

        if (currentState == PreviewState.ERROR) {
            ErrorState(boldFont)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                SharedByHeader(
                    archiveThumbURL = archiveThumbURL,
                    rawAccountName = rawAccountName,
                    rawArchiveName = rawArchiveName,
                    mediumFont = mediumFont,
                    regularFont = regularFont
                )

                Box(modifier = Modifier.fillMaxWidth()) {

                    RecordsLayout(
                        records = records,
                        accessType = accessType,
                        isBusy = isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (hasBlurredContent) Modifier.blur(12.dp) else Modifier)
                    )

                    if (hasBlurredContent) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xA3F4F6FD))
                        )
                    }
                }
            }
        }

        if (currentState != PreviewState.ERROR) {
            ArchivePickerCard(
                selectedArchive = selectedArchive,
                onClick = { showArchivePickerSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 48.dp, vertical = 32.dp)
                    .fillMaxWidth()
            )
        }

        if (showArchivePickerSheet) {
            ArchivePickerBottomSheet(
                archives = archives,
                selectedArchive = selectedArchive,
                onArchiveSelected = { viewModel.onArchiveSelected(it) },
                onDismiss = { showArchivePickerSheet = false }
            )
        }

        if (isBusy) {
            CircularProgressIndicator(overlayColor = OverlayColor.LIGHT)
        }
    }
}

@Composable
private fun ErrorState(
    boldFont: FontFamily
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.share_preview_link_unavailable),
            fontSize = 18.sp,
            fontFamily = boldFont,
            color = colorResource(R.color.blue900),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun SharedByHeader(
    archiveThumbURL: String,
    rawAccountName: String,
    rawArchiveName: String,
    mediumFont: FontFamily,
    regularFont: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (archiveThumbURL.isNotEmpty()) {
            AsyncImage(
                model = archiveThumbURL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colorResource(R.color.blue100))
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            val text = buildAnnotatedString {
                withStyle(SpanStyle(fontFamily = regularFont)) {
                    append(stringResource(R.string.share_preview_shared_by))
                    append(" ")
                }
                withStyle(SpanStyle(fontFamily = mediumFont, fontWeight = FontWeight.Bold)) {
                    append(rawAccountName)
                }
                withStyle(SpanStyle(fontFamily = regularFont)) {
                    append(" ")
                    append(stringResource(R.string.share_preview_from))
                }
            }

            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colorResource(R.color.blue600),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = rawArchiveName,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = regularFont,
                color = colorResource(R.color.blue900),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecordsLayout(
    records: List<Record>,
    accessType: AccessType?,
    isBusy: Boolean,
    modifier: Modifier = Modifier
) {
    val gap = 8.dp

    if (!isBusy && records.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.folder_is_empty),
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                fontSize = 18.sp,
                color = colorResource(R.color.blue900),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {

        // ✅ Single item special case
        if (records.size == 1 && records.first().type != RecordType.FOLDER) {
            val record = records.first()

            RecordGridItem(
                record = record,
                accessType = accessType,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            return@Column
        }

        // Always use 4-slot layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {

            // Left column (2 stacked)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {

                // Slot 0
                records.getOrNull(0)?.let {
                    RecordGridItem(
                        record = it,
                        accessType = accessType,
                        modifier = Modifier.aspectRatio(1f)
                    )
                } ?: Spacer(modifier = Modifier.aspectRatio(1f))

                // Slot 2
                records.getOrNull(2)?.let {
                    RecordGridItem(
                        record = it,
                        accessType = accessType,
                        modifier = Modifier.aspectRatio(1f)
                    )
                } ?: Spacer(modifier = Modifier.aspectRatio(1f))
            }

            // Right tall item (slot 1)
            records.getOrNull(1)?.let {
                RecordGridItem(
                    record = it,
                    accessType = accessType,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } ?: Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Bottom item (slot 3)
        records.getOrNull(3)?.let {
            RecordGridItem(
                record = it,
                accessType = accessType,
                isLastItem = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RecordGridItem(
    record: Record,
    accessType: AccessType?,
    isLastItem: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thumbUrl = record.thumbURL2000 ?: ""

    val isBlurred = accessType != AccessType.ANYONE_CAN_VIEW

    Box(
        modifier = modifier
            .clip(
                if (isLastItem) {
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                } else {
                    RoundedCornerShape(12.dp)
                }
            )
            .background(colorResource(R.color.white))
    ) {

        when {
            isBlurred && record.localDrawableRes != null -> {
                Image(
                    painter = painterResource(record.localDrawableRes!!),
                    contentDescription = record.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            thumbUrl.isNotEmpty() && !isBlurred -> {
                AsyncImage(
                    model = thumbUrl,
                    contentDescription = record.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder_purple_gradient),
                        contentDescription = record.displayName,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(72.dp)
                    )

                    if (!record.displayName.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = record.displayName ?: "",
                            fontFamily = FontFamily(Font(R.font.usual_regular)),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = colorResource(R.color.blue900),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}