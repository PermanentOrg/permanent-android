package org.permanent.permanent.ui.recordMenu.compose

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.ui.boundsOnScreen
import org.permanent.permanent.ui.recordMenu.RecordMenuAnchor
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.composeComponents.AccessRoleLabel
import org.permanent.permanent.ui.composeComponents.AccessRoleLabelColor
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.PendingInvitationBadge
import org.permanent.permanent.ui.composeComponents.SettingsMenuItem
import org.permanent.permanent.viewmodels.RecordMenuItem
import org.permanent.permanent.viewmodels.RecordMenuViewModel
import java.util.Locale

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
    val archiveThumb by viewModel.archiveThumb.collectAsState()
    val archiveName by viewModel.archiveName.collectAsState()
    val accessRole by viewModel.accessRole.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val pendingInvitationCount = viewModel.pendingInvitationCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
            RecordMenuHeader(
                recordThumbURL = recordThumbURL,
                recordName = recordName,
                recordSize = recordSize,
                recordDate = recordDate,
                archiveThumb = archiveThumb,
                archiveName = archiveName,
                accessRole = accessRole,
                onCloseClick = onClose
            )

            RecordMenuItems(
                menuItems = menuItems,
                pendingInvitationCount = pendingInvitationCount,
                onItemClick = onItemClick,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        if (isBusyState) BusyOverlay()
    }
}

@Composable
fun RecordMenuPopup(
    viewModel: RecordMenuViewModel,
    anchor: RecordMenuAnchor,
    onItemClick: (RecordMenuItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val isBusyState by viewModel.isBusyState.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val headerItems = remember(menuItems) { popupHeaderItems.filter { it in menuItems } }
    val listItems = remember(menuItems) { menuItems - popupHeaderItems.toSet() }
    val view = LocalView.current
    val margin = with(LocalDensity.current) { 8.dp.roundToPx() }
    var origin by remember { mutableStateOf<IntOffset?>(null) }
    val washOutColor = colorResource(R.color.whiteTransparent)

    Layout(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { origin = view.boundsOnScreen().let { IntOffset(it.left, it.top) } }
            .drawBehind {
                val windowOrigin = origin ?: return@drawBehind
                fun Rect.inWindow() = ComposeRect(
                    (left - windowOrigin.x).toFloat(), (top - windowOrigin.y).toFloat(),
                    (right - windowOrigin.x).toFloat(), (bottom - windowOrigin.y).toFloat()
                )
                // One path, so overlapping shapes are washed out once
                val washOut = Path().apply {
                    anchor.washOutBounds.forEach { addRect(it.inWindow()) }
                    anchor.roundWashOutBounds.forEach { addOval(it.inWindow()) }
                }
                drawPath(washOut, washOutColor)
            }
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        content = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 8.dp,
            ) {
                Box(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .widthIn(min = 240.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        if (headerItems.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                headerItems.forEach { item ->
                                    RecordMenuHeaderAction(
                                        item = item,
                                        modifier = Modifier.weight(1f)
                                    ) { onItemClick(item) }
                                }
                            }
                            if (listItems.isNotEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = colorResource(R.color.blue50)
                                )
                            }
                        }
                        RecordMenuItems(
                            menuItems = listItems,
                            onItemClick = onItemClick,
                            itemVerticalPadding = 12.dp,
                            dividerVerticalPadding = 0.dp
                        )
                    }

                    if (isBusyState) BusyOverlay()
                }
            }
        }
    ) { measurables, constraints ->
        // The anchor is in screen coordinates and the window may be inset, so wait for its origin
        val windowOrigin = origin
            ?: return@Layout layout(constraints.maxWidth, constraints.maxHeight) {}
        val top = anchor.bounds.top - windowOrigin.y
        val bottom = anchor.bounds.bottom - windowOrigin.y
        val spaceAbove = top - margin
        val spaceBelow = constraints.maxHeight - bottom - margin
        val opensBelow = spaceBelow >= spaceAbove
        val placeable = measurables.first().measure(
            Constraints(
                maxWidth = (constraints.maxWidth - 2 * margin).coerceAtLeast(0),
                maxHeight = (if (opensBelow) spaceBelow else spaceAbove).coerceAtLeast(0)
            )
        )
        val x = (anchor.bounds.right - windowOrigin.x - margin - placeable.width)
            .coerceIn(margin, (constraints.maxWidth - margin - placeable.width).coerceAtLeast(margin))
        val y = if (opensBelow) bottom else top - placeable.height

        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(x, y) }
    }
}

@Composable
private fun BoxScope.BusyOverlay() {
    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(overlayColor = OverlayColor.LIGHT)
    }
}

private val popupHeaderItems = listOf(RecordMenuItem.Copy, RecordMenuItem.Move, RecordMenuItem.Share)

@Composable
private fun RecordMenuHeaderAction(
    item: RecordMenuItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val (iconRes, labelRes) = when (item) {
        RecordMenuItem.Copy -> R.drawable.ic_copy_primary to R.string.copy
        RecordMenuItem.Move -> R.drawable.ic_move_primary to R.string.move
        else -> R.drawable.ic_share_primary to R.string.share_button
    }
    val color = colorResource(R.color.colorPrimary)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(labelRes),
            fontSize = 13.sp,
            color = color,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            maxLines = 1
        )
    }
}

@Composable
fun RecordMenuItems(
    menuItems: List<RecordMenuItem>,
    pendingInvitationCount: Int = 0,
    onItemClick: (RecordMenuItem) -> Unit,
    modifier: Modifier = Modifier,
    itemVerticalPadding: Dp = 16.dp,
    dividerVerticalPadding: Dp = 16.dp,
) {
    Column(modifier = modifier) {
        menuItems.forEach { item ->
            when (item) {
                RecordMenuItem.Share -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_share_primary),
                    text = stringResource(R.string.share_and_manage_access),
                    trailing = if (pendingInvitationCount > 0) {
                        { PendingInvitationBadge(count = pendingInvitationCount) }
                    } else null,
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.Publish -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_publish_primary),
                    text = stringResource(R.string.publish_on_the_web),
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.SendACopy -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_send_primary),
                    text = stringResource(R.string.send_a_copy),
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.GetLink -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_link_primary),
                    text = stringResource(R.string.get_link),
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.Download -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_download_primary),
                    text = stringResource(R.string.download),
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.Rename -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_rename_primary),
                    text = stringResource(R.string.rename),
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.Move -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_move_primary),
                    text = stringResource(R.string.move_to_another_folder),
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.Copy -> SettingsMenuItem(
                    iconResource = painterResource(id = R.drawable.ic_copy_primary),
                    text = stringResource(R.string.copy_to_another_folder),
                    verticalPadding = itemVerticalPadding,
                ) { onItemClick(item) }

                RecordMenuItem.Delete -> {
                    if (menuItems.size > 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = dividerVerticalPadding),
                            color = colorResource(R.color.blue50)
                        )
                    }
                    SettingsMenuItem(
                        iconResource = painterResource(id = R.drawable.ic_delete_large_red),
                        text = stringResource(R.string.delete),
                        itemColor = colorResource(R.color.error500),
                        verticalPadding = itemVerticalPadding,
                    ) { onItemClick(item) }
                }

                RecordMenuItem.LeaveShare -> {
                    if (menuItems.size > 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = dividerVerticalPadding),
                            color = colorResource(R.color.blue50)
                        )
                    }
                    SettingsMenuItem(
                        iconResource = painterResource(id = R.drawable.ic_leave_share_red),
                        text = stringResource(R.string.leave_share),
                        itemColor = colorResource(R.color.error500),
                        verticalPadding = itemVerticalPadding,
                    ) { onItemClick(item) }
                }

                else -> {}
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
    archiveThumb: String,
    archiveName: String,
    accessRole: AccessRole,
    onCloseClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .background(colorResource(R.color.blue25))
            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
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
                    ), maxLines = 1, overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val infoText =
                    listOf(recordSize, recordDate).filter { it.isNotBlank() }.joinToString(" • ")

                Text(
                    text = infoText, style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.blue400),
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Close Icon
            if (onCloseClick != null) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_light_blue),
                        contentDescription = "Close",
                        tint = colorResource(R.color.blue400),
                    )
                }
            }
        }

        if (archiveThumb.isNotEmpty() && archiveName.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 24.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                AsyncImage(
                    model = archiveThumb,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                Spacer(modifier = Modifier.width(24.dp))

                // Texts
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = stringResource(R.string.from).uppercase(Locale.getDefault()),
                        style = TextStyle(
                            fontSize = 10.sp,
                            lineHeight = 8.sp,
                            fontFamily = FontFamily(Font(R.font.usual_regular)),
                            color = colorResource(R.color.blue400),
                        )
                    )

                    Text(
                        text = archiveName, style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily(Font(R.font.usual_medium)),
                            color = colorResource(R.color.blue900),
                        ), maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                AccessRoleLabel(accessRole = accessRole, color = AccessRoleLabelColor.LIGHT)
            }
        }
    }
}