@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.dashboard.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.ui.archiveOnboarding.compose.UIArchive
import org.permanent.permanent.ui.archiveOnboarding.compose.archiveTypePickerItems
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.MenuItem
import org.permanent.permanent.ui.dashboard.NavyTitleGradient
import org.permanent.permanent.ui.dashboard.PurpleOrangeTitleGradient
import org.permanent.permanent.ui.dashboard.UsualFontFamily

/**
 * Create-archive bottom sheet, matching the Figma "Create your first Archive" modal 1:1
 * (frames 25369-22390 / 25369-27248).
 *
 * Reuses the onboarding [ArchiveTypePickerBottomSheet] + [archiveTypePickerItems] for the
 * "Archive type" picker that opens when the type field is tapped, and [GradientDisplayTitle]
 * for the serif hero title. The chosen type + name drive DashboardViewModel.createArchive().
 */
@Composable
fun CreateArchiveBottomSheet(
    isCreating: Boolean,
    onCreate: (name: String, type: ArchiveType) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val blue25 = colorResource(R.color.blue25)
    val blue50 = colorResource(R.color.blue50)
    val blue400 = colorResource(R.color.blue400)
    val blue900 = colorResource(R.color.blue900)

    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<UIArchive?>(null) }
    var showTypePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = blue25,
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
        dragHandle = null,
        // Edge-to-edge so the loading overlay covers the full sheet (incl. the bottom inset).
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.93f)) {
          Column(modifier = Modifier.fillMaxSize()) {
            SheetHeader(
                title = stringResource(R.string.dashboard_create_first_archive_button),
                onClose = onDismiss
            )

            // Body — 32 horizontal padding, 16 gap, button pinned to bottom.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 32.dp, end = 32.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // "What do you plan to capture and preserve?" — serif display, navy gradient.
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GradientDisplayTitle(
                        text = buildAnnotatedString {
                            append("What do you plan to ")
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("capture") }
                            append(" and ")
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("preserve?") }
                        },
                        gradient = NavyTitleGradient
                    )
                }

                // Type field — white bordered field; opens the reused type picker.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, blue50, RoundedCornerShape(12.dp))
                        .clickable { showTypePicker = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        GradientGlyph(
                            painter = painterResource(id = selected?.icon ?: R.drawable.ic_heart_white),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = selected?.let { stringResource(it.title) }
                            ?: stringResource(R.string.dashboard_archive_type_default),
                        color = blue900,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 24.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_angles_up_down),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Name field — white bordered field with gradient archive icon + clear button.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, blue50, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_archive_name_gradient),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = blue900,
                            fontFamily = UsualFontFamily,
                            // Entered name is Medium, matching the type-selector value ("Personal
                            // life journey") above it, per Figma.
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(blue900),
                        decorationBox = { inner ->
                            if (name.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.dashboard_archive_name_hint),
                                    color = blue400,
                                    fontFamily = UsualFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 24.sp
                                )
                            }
                            inner()
                        }
                    )
                    if (name.isNotEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_circle_xmark_grey),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { name = "" }
                        )
                    }
                }

                // Lock note row.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lock_green),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.dashboard_create_archive_privacy_note),
                        color = blue400,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Visible (solid) from the start, per design; the ViewModel ignores a blank name.
                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.DARK,
                    text = stringResource(R.string.dashboard_create_archive_confirm),
                    fontSize = 14.sp,
                    icon = null,
                    enabled = !isCreating,
                    onButtonClick = { onCreate(name, selected?.type ?: ArchiveType.PERSON) }
                )
            }
          }
          // Existing spinner overlay shown while the backend creates the archive.
          if (isCreating) {
              CircularProgressIndicator()
          }
        }
    }

    if (showTypePicker) {
        // "Archive type" picker — same header + height as this sheet; reuses the onboarding
        // item list (archiveTypePickerItems + MenuItem).
        val typeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showTypePicker = false },
            sheetState = typeSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
            dragHandle = null
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.93f)) {
                SheetHeader(
                    title = stringResource(R.string.archive_type),
                    onClose = { showTypePicker = false }
                )
                // Fixed top divider — stays under the header while the list scrolls.
                HorizontalDivider(color = colorResource(R.color.blue50))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(archiveTypePickerItems().size) { index ->
                        val item = archiveTypePickerItems()[index]
                        val isSelected = selected != null &&
                            selected?.type == item.type && selected?.title == item.title
                        if (index > 0) HorizontalDivider(color = colorResource(R.color.blue50))
                        MenuItem(
                            iconResource = painterResource(id = item.icon),
                            title = stringResource(item.title),
                            subtitle = stringResource(item.description),
                            isSelected = isSelected
                        ) {
                            selected = item
                            showTypePicker = false
                        }
                    }
                }
            }
        }
    }
}

/** Shared bottom-sheet header: invisible left slot · centered title · circular close button. */
@Composable
private fun SheetHeader(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            color = colorResource(R.color.blue900),
            fontFamily = UsualFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.16).sp,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_close_middle_grey),
                contentDescription = stringResource(R.string.menu_toolbar_close),
                colorFilter = ColorFilter.tint(colorResource(R.color.blue900)),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Renders an icon glyph filled with the purple→orange gradient (SrcAtop over the glyph mask). */
@Composable
private fun GradientGlyph(painter: Painter, modifier: Modifier) {
    val brush = Brush.linearGradient(PurpleOrangeTitleGradient)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .graphicsLayer(alpha = 0.99f)
            .drawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
            }
    )
}
