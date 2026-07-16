@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.shares.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.ui.archiveOnboarding.compose.ArchiveTypePickerContent
import org.permanent.permanent.ui.archiveOnboarding.compose.UIArchive
import org.permanent.permanent.ui.archiveOnboarding.compose.archiveTypePickerItems
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor

private enum class CreateArchiveStep { FORM, TYPE_PICKER }

@Composable
fun CreateArchiveBottomSheet(
    isBusy: Boolean,
    onSubmit: (name: String, type: ArchiveType) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val items = remember { archiveTypePickerItems() }
    var step by rememberSaveable { mutableStateOf(CreateArchiveStep.FORM) }
    var selectedType by remember { mutableStateOf(items[0]) }
    var name by rememberSaveable { mutableStateOf("") }

    val dismiss: () -> Unit = {
        if (!isBusy) {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = colorResource(R.color.white),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.92f)
                .fillMaxWidth()
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (step) {
                    CreateArchiveStep.FORM -> CreateArchiveFormContent(
                        name = name,
                        selectedType = selectedType,
                        onNameChange = { name = it },
                        onTypeRowClick = { step = CreateArchiveStep.TYPE_PICKER },
                        onCreate = {
                            if (name.isNotBlank() && !isBusy) {
                                onSubmit(name.trim(), selectedType.type)
                            }
                        },
                        onDismiss = dismiss,
                        isBusy = isBusy
                    )

                    CreateArchiveStep.TYPE_PICKER -> ArchiveTypePickerContent(
                        selected = selectedType,
                        items = items,
                        onSelect = {
                            selectedType = it
                            step = CreateArchiveStep.FORM
                        },
                        onDismiss = dismiss,
                        onBack = { step = CreateArchiveStep.FORM },
                        headerPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                        sheetState = sheetState
                    )
                }
            }

            if (isBusy) {
                CircularProgressIndicator(overlayColor = OverlayColor.LIGHT)
            }
        }
    }
}

@Composable
private fun ColumnScope.CreateArchiveFormContent(
    name: String,
    selectedType: UIArchive,
    onNameChange: (String) -> Unit,
    onTypeRowClick: () -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
    isBusy: Boolean
) {
    val mediumFont = FontFamily(Font(R.font.usual_medium))
    val regularFont = FontFamily(Font(R.font.usual_regular))
    val boldFont = FontFamily(Font(R.font.usual_bold))

    val blue900 = colorResource(R.color.blue900)
    val blue400 = colorResource(R.color.blue400)
    val blue200 = colorResource(R.color.blue200)
    val blue50 = colorResource(R.color.blue50)
    val blue25 = colorResource(R.color.blue25)
    val white = colorResource(R.color.white)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(18.dp))

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            text = stringResource(R.string.share_preview_create_archive_title),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = boldFont,
            color = blue900
        )

        IconButton(onClick = onDismiss, enabled = !isBusy) {
            Icon(
                painter = painterResource(R.drawable.ic_close_middle_grey),
                contentDescription = stringResource(R.string.menu_toolbar_close),
                tint = blue200,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    HorizontalDivider(thickness = 1.dp, color = blue50)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(blue25)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.share_preview_create_archive_question),
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = FontFamily(Font(R.font.usual_light, FontWeight(350))),
            fontWeight = FontWeight(350),
            color = blue900
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(white, RoundedCornerShape(12.dp))
                .border(1.dp, blue50, RoundedCornerShape(12.dp))
                .clickable(enabled = !isBusy) { onTypeRowClick() }
                .padding(start = 8.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(blue25, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(selectedType.icon),
                    contentDescription = null,
                    tint = blue900,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(selectedType.title),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = boldFont,
                color = blue900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_select_light_blue),
                contentDescription = null,
                tint = blue200,
                modifier = Modifier.size(12.dp)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(white)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.share_preview_name_your_new_archive),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontFamily = mediumFont,
            color = blue900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(white, RoundedCornerShape(12.dp))
                .border(1.dp, blue50, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArchiveThumbnail(
                archive = null,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.share_preview_the_prefix) + " ",
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = regularFont,
                color = blue900
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (name.isEmpty()) {
                    Text(
                        text = stringResource(selectedType.title),
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = regularFont,
                        color = blue400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    enabled = !isBusy,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = boldFont,
                        color = blue900
                    ),
                    cursorBrush = SolidColor(blue900),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = " " + stringResource(R.string.share_preview_archive_suffix),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = regularFont,
                color = blue900
            )

            Spacer(modifier = Modifier.width(8.dp))
        }
    }

    Spacer(modifier = Modifier.weight(1f))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(white)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        val canSubmit = name.isNotBlank() && !isBusy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    if (canSubmit) blue900 else blue900.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .clickable(enabled = canSubmit) { onCreate() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.share_preview_create_button),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = mediumFont,
                color = white
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(blue25, RoundedCornerShape(12.dp))
                .clickable(enabled = !isBusy) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.share_preview_cancel_button),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = mediumFont,
                color = blue900
            )
        }
    }
}
