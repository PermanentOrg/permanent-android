package org.permanent.permanent.ui.myFiles.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.NavigationFolderIdentifier
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.RecordType
import org.permanent.permanent.ui.composeComponents.AnimatedTemporarySnackbar
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.TemporarySnackbarType
import org.permanent.permanent.viewmodels.NewFolderViewModel
import org.permanent.permanent.viewmodels.RenameRecordViewModel

@Composable
fun RenameScreen(
    viewModel: RenameRecordViewModel,
    record: Record,
    onCompleted: () -> Unit,
    onClose: () -> Unit
) {
    val name by viewModel.currentRecordName.collectAsState()
    val isEnabled by viewModel.isRenameEnabled.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()

    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarType by remember { mutableStateOf(TemporarySnackbarType.ERROR) }

    LaunchedEffect(Unit) {
        viewModel.onRecordRenamed.collect { onCompleted() }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            snackbarType = TemporarySnackbarType.ERROR
            snackbarMessage = message
        }
    }

    val isFolder = record.type == RecordType.FOLDER
    NameInputLayout(
        title = stringResource(R.string.rename_file_title),
        buttonLabel = stringResource(R.string.rename),
        name = name,
        onNameChange = viewModel::onNameChanged,
        onConfirm = { viewModel.renameRecord(record) },
        isConfirmEnabled = isEnabled,
        isBusy = isBusy,
        onClose = onClose,
        thumbnailUrl = if (isFolder) null else record.thumbnail256 ?: record.thumbURL200,
        iconRes = if (isFolder) R.drawable.ic_folder_barney_purple else null,
        snackbarMessage = snackbarMessage,
        snackbarType = snackbarType,
        onSnackbarDismiss = { snackbarMessage = "" }
    )
}

@Composable
fun NewFolderScreen(
    viewModel: NewFolderViewModel,
    folderIdentifier: NavigationFolderIdentifier?,
    onCompleted: () -> Unit,
    onClose: () -> Unit
) {
    val name by viewModel.currentFolderName.collectAsState()
    val isEnabled by viewModel.isCreateEnabled.collectAsState()
    val isBusy by viewModel.isBusy.collectAsState()

    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarType by remember { mutableStateOf(TemporarySnackbarType.ERROR) }

    LaunchedEffect(Unit) {
        viewModel.onFolderCreated.collect { onCompleted() }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            snackbarType = TemporarySnackbarType.ERROR
            snackbarMessage = message
        }
    }

    NameInputLayout(
        title = stringResource(R.string.new_folder_title),
        buttonLabel = stringResource(R.string.create_button),
        name = name,
        onNameChange = viewModel::onNameChanged,
        onConfirm = { viewModel.createNewFolder(folderIdentifier) },
        isConfirmEnabled = isEnabled,
        isBusy = isBusy,
        onClose = onClose,
        hint = stringResource(R.string.new_folder_hint),
        iconRes = R.drawable.ic_folder_barney_purple,
        snackbarMessage = snackbarMessage,
        snackbarType = snackbarType,
        onSnackbarDismiss = { snackbarMessage = "" }
    )
}

@Composable
private fun NameInputLayout(
    title: String,
    buttonLabel: String,
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean,
    isBusy: Boolean,
    onClose: () -> Unit,
    thumbnailUrl: String? = null,
    hint: String? = null,
    iconRes: Int? = null,
    snackbarMessage: String = "",
    snackbarType: TemporarySnackbarType = TemporarySnackbarType.SUCCESS,
    onSnackbarDismiss: () -> Unit = {}
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            ModalHeader(title = title, onClose = onClose)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp)
            ) {
                NameInputField(
                    name = name,
                    onNameChange = onNameChange,
                    thumbnailUrl = thumbnailUrl,
                    hint = hint,
                    iconRes = iconRes
                )

                Spacer(modifier = Modifier.height(24.dp))

                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.DARK,
                    text = buttonLabel,
                    icon = null,
                    enabled = isConfirmEnabled,
                    disabledColor = colorResource(R.color.blue200),
                    onButtonClick = onConfirm
                )

                if (isBusy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(overlayColor = OverlayColor.LIGHT)
                    }
                }
            }
        }

        AnimatedTemporarySnackbar(
            message = snackbarMessage,
            type = snackbarType,
            onButtonClick = onSnackbarDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
fun ModalHeader(title: String, onClose: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.usual_bold)),
                    color = colorResource(R.color.blue900),
                    letterSpacing = (-0.01).sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_light_blue),
                    contentDescription = stringResource(R.string.button_cancel),
                    tint = colorResource(R.color.blue200)
                )
            }
        }

        HorizontalDivider(color = colorResource(R.color.blue50), thickness = 1.dp)
    }
}

@Composable
private fun NameInputField(
    name: String,
    onNameChange: (String) -> Unit,
    thumbnailUrl: String? = null,
    hint: String? = null,
    iconRes: Int? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, colorResource(R.color.blue100), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))
        } else if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (name.isEmpty() && hint != null) {
                Text(
                    text = hint,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.blue200)
                    )
                )
            }
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue900)
                ),
                cursorBrush = SolidColor(colorResource(R.color.blue900)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (name.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onNameChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_circle),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
    }
}
