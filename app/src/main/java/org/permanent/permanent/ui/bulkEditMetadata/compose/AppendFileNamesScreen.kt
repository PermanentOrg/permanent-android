package org.permanent.permanent.ui.bulkEditMetadata.compose

import CustomDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditFileNamesUIState

@Composable
fun AppendFileNamesScreen(uiState: EditFileNamesUIState,
                          append: (String, Boolean) -> Unit,
                          applyChanges: (String, Boolean) -> Unit,
                          cancel: () -> Unit) {

    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current
    val middleGrey = Color(ContextCompat.getColor(context, R.color.middleGrey))
    val superLightBlue = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val blue900 = Color(ContextCompat.getColor(context, R.color.blue900))
    val openAlertDialog = remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var dropdownSize by remember { mutableStateOf(Size.Zero)}

    val options = listOf(
        AppendFilesOptions.BEFORE,
        AppendFilesOptions.AFTER
    )
    var selectedOption by remember { mutableStateOf(AppendFilesOptions.BEFORE) }

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(id = R.string.text).uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = semiBoldFont,
                    color = middleGrey)
            )
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = superLightBlue,
                        shape = RoundedCornerShape(size = 2.dp)
                    )
                    .height(48.dp)
                    .background(
                        color = superLightBlue.copy(0.5f),
                        shape = RoundedCornerShape(size = 2.dp)
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        append(text, selectedOption.appendBefore())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textStyle = TextStyle(
                        fontFamily = regularFont,
                        fontSize = 13.sp,
                        color = blue900,
                        lineHeight = 16.sp
                    ),
                    singleLine = true
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(id = R.string.where).uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = semiBoldFont,
                    color = middleGrey)
            )
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = superLightBlue,
                        shape = RoundedCornerShape(size = 2.dp)
                    )
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        //This value is used to assign to the DropDown the same width
                        dropdownSize = coordinates.size.toSize()
                    }
                    .height(48.dp)
                    .background(
                        color = superLightBlue.copy(0.5f),
                        shape = RoundedCornerShape(size = 2.dp)
                    )
                    .clickable {
                        expanded = !expanded
                    },
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = stringResource(id = selectedOption.titleID),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textStyle = TextStyle(
                        fontFamily = regularFont,
                        fontSize = 13.sp,
                        color = blue900,
                        lineHeight = 16.sp
                    ),
                    singleLine = true,
                    enabled = false
                )
                DropdownMenu(
                    modifier = Modifier
                        .background(Color.White)
                        .width(with(LocalDensity.current) { dropdownSize.width.toDp() }),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach {
                        DropdownMenuItem(
                            modifier = Modifier
                                .background(Color.White),
                            text = { Text(stringResource(id = it.titleID)) },
                            onClick = {
                                expanded = false
                                selectedOption = it
                                append(text, selectedOption.appendBefore())
                            })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1.0f))
        EditFileNamesFooter(uiState,
            cancel = {
                cancel()
            }, apply = {
                openAlertDialog.value = true
            })
        when {
            openAlertDialog.value -> {
                CustomDialog(
                    title = stringResource(id = R.string.modify_file_names),
                    subtitle = stringResource(id = R.string.replace_confirmation_substring, uiState.recordsNumber),
                    okButtonText = stringResource(id = R.string.modify),
                    cancelButtonText = stringResource(id = R.string.button_cancel),
                    onConfirm = {
                        openAlertDialog.value = false
                        applyChanges(text, selectedOption.appendBefore())
                    }) {
                    openAlertDialog.value = false
                }
            }
        }
    }
}

private enum class AppendFilesOptions(val titleID: Int) {
    BEFORE(R.string.before_name),
    AFTER(R.string.after_name);

    fun appendBefore(): Boolean {
        return this == BEFORE
    }
}