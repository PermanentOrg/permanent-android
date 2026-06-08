package org.permanent.permanent.ui.shareManagement.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive

/** Square archive thumbnail, falling back to the multicolor placeholder when no image is set. */
@Composable
fun ArchiveThumbnail(archive: Archive?, modifier: Modifier = Modifier) {
    val thumbURL = archive?.thumbnail256 ?: archive?.thumbURL200
    if (thumbURL?.isNotEmpty() == true) {
        AsyncImage(
            model = thumbURL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    } else {
        Icon(
            painter = painterResource(id = R.drawable.ic_archive_placeholder_multicolor),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    }
}

/** Drawable for the green role icon shown across the share-management screens. */
@DrawableRes
fun AccessRole.iconRes(): Int = when (this) {
    AccessRole.VIEWER -> R.drawable.ic_viewer_green
    AccessRole.CONTRIBUTOR -> R.drawable.ic_contributor_green
    AccessRole.EDITOR -> R.drawable.ic_editor_green
    AccessRole.CURATOR, AccessRole.MANAGER -> R.drawable.ic_curator_green
    AccessRole.OWNER -> R.drawable.ic_owner_green
}

/** A tappable "icon tile + label + chevron" row used to enter the grant-access sub-flows. */
@Composable
fun GrantAccessEntryRow(
    @DrawableRes iconResId: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colorResource(R.color.colorPrimary)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = colorResource(R.color.white),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.blue900),
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_select_light_blue),
            contentDescription = null,
            tint = colorResource(R.color.blue200)
        )
    }
}