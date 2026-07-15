package org.permanent.permanent.ui.shareManagement.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
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

/**
 * The shadowed rounded search/filter field used by the archive selection pages, with the
 * gradient search icon tile and a clear button that appears while there is input.
 */
@Composable
fun ArchiveSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    // The email search field floats on a shadow; the past-shares filter sits flat
    // on a blue25 band (Figma node 21736:15852).
    elevated: Boolean = true,
    onClear: () -> Unit = { onValueChange("") },
) {
    val primaryColor = colorResource(R.color.colorPrimary)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 24.dp)
            .then(
                if (elevated) Modifier.shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = primaryColor,
                    spotColor = primaryColor
                ) else Modifier
            )
            .background(colorResource(R.color.white), RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(R.color.blue50),
                shape = RoundedCornerShape(12.dp)
            )
            .height(48.dp)
            .padding(start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorResource(R.color.blue25)),
            contentAlignment = Alignment.Center
        ) {
            GradientSearchIcon()
        }

        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue900),
                ),
                cursorBrush = SolidColor(primaryColor),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily(Font(R.font.usual_regular)),
                                color = colorResource(R.color.blue400),
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.blue200)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_white),
                        contentDescription = "Clear",
                        tint = colorResource(R.color.white),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientSearchIcon() {
    val brush = Brush.linearGradient(
        colors = listOf(colorResource(R.color.barneyPurple), colorResource(R.color.colorAccent)),
        start = Offset(0f, 0f),
        end = Offset(48f, 48f)
    )
    Icon(
        painter = painterResource(id = R.drawable.ic_search_middle_grey),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier
            .size(22.dp)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
            }
    )
}

/**
 * An archive row in a selectable list (email search results, past shares). Rows whose archive
 * already has access to the share are disabled: scrimmed thumbnail, greyed name, a green
 * "already has access" caption and a check icon instead of the chevron.
 */
@Composable
fun ArchiveResultRow(archive: Archive, hasAccess: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hasAccess) Modifier else Modifier.clickable { onClick() })
            .padding(start = 24.dp, end = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            ArchiveThumbnail(archive)
            if (hasAccess) {
                // De-emphasize the thumbnail with a white 16% scrim (Figma: White / 16%).
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = archive.fullName ?: "",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(if (hasAccess) R.color.blue400 else R.color.blue900),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (hasAccess) {
                Text(
                    text = stringResource(R.string.already_has_access_to_this_share),
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.success500),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            painter = painterResource(
                id = if (hasAccess) R.drawable.ic_check_circle_filled
                else R.drawable.ic_arrow_select_light_blue
            ),
            contentDescription = null,
            tint = colorResource(R.color.blue200),
            modifier = if (hasAccess) Modifier.size(18.dp) else Modifier
        )
    }
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
/** Uppercase 10sp section title used across the share-management screens. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.toUpperCase(Locale.current),
        modifier = modifier,
        style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 8.sp,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            color = colorResource(R.color.blue900),
            letterSpacing = 1.6.sp,
        )
    )
}

/** Centered secondary message used for error states in the archive selection pages. */
@Composable
fun CenteredMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue400),
            )
        )
    }
}
