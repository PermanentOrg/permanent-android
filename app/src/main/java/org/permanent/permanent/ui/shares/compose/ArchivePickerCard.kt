package org.permanent.permanent.ui.shares.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive

@Composable
fun ArchivePickerCard(
    selectedArchive: Archive?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val mediumFont = FontFamily(Font(R.font.usual_medium))
    val regularFont = FontFamily(Font(R.font.usual_regular))

    Column(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(colorResource(R.color.white))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable(enabled = enabled) { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArchiveThumbnail(
                archive = selectedArchive,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.share_preview_view_shared_items_in),
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    fontFamily = regularFont,
                    color = colorResource(R.color.blue600),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val bodyText = if (selectedArchive != null) {
                    formatArchiveName(
                        fullName = selectedArchive.fullName.orEmpty(),
                        regularFont = regularFont,
                        mediumFont = mediumFont
                    )
                } else {
                    AnnotatedString(stringResource(R.string.share_preview_select_archive))
                }

                Text(
                    text = bodyText,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = mediumFont,
                    color = colorResource(R.color.blue900),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down_white),
                contentDescription = null,
                tint = colorResource(R.color.blue200),
                modifier = Modifier.size(24.dp)
            )
        }

        if (content != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun formatArchiveName(
    fullName: String,
    regularFont: FontFamily,
    mediumFont: FontFamily
): AnnotatedString {
    val prefix = stringResource(R.string.share_preview_the_prefix) + " "
    val suffix = " " + stringResource(R.string.share_preview_archive_suffix)
    val hasPrefix = fullName.startsWith(prefix)
    val hasSuffix = fullName.endsWith(suffix)
    val middle = fullName
        .let { if (hasPrefix) it.removePrefix(prefix) else it }
        .let { if (hasSuffix) it.removeSuffix(suffix) else it }

    return buildAnnotatedString {
        if (hasPrefix) {
            withStyle(SpanStyle(fontFamily = regularFont)) { append(prefix) }
        }
        withStyle(SpanStyle(fontFamily = mediumFont)) { append(middle) }
        if (hasSuffix) {
            withStyle(SpanStyle(fontFamily = regularFont)) { append(suffix) }
        }
    }
}

@Composable
internal fun ArchiveThumbnail(
    archive: Archive?,
    modifier: Modifier = Modifier
) {
    val thumbUrl = archive?.thumbnail256 ?: archive?.thumbURL200
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.Transparent)
    ) {
        if (!thumbUrl.isNullOrEmpty()) {
            AsyncImage(
                model = thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_archive_placeholder),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
