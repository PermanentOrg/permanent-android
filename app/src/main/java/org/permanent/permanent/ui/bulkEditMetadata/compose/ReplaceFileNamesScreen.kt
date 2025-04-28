package org.permanent.permanent.ui.bulkEditMetadata.compose

import CustomDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditFileNamesUIState

@Composable
fun ReplaceFileNamesScreen(uiState: EditFileNamesUIState,
                           replace: (String, String) -> Unit,
                           applyChanges: (String, String) -> Unit,
                           cancel: () -> Unit) {
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val middleGrey = Color(ContextCompat.getColor(context, R.color.middleGrey))
    val superLightBlue = Color(ContextCompat.getColor(context, R.color.blue25))
    val blue900 = Color(ContextCompat.getColor(context, R.color.blue900))
    val openAlertDialog = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(id = R.string.find).uppercase(),
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
                    value = findText,
                    onValueChange = {
                        findText = it
                        replace(findText, replaceText)
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
            Text(text = stringResource(id = R.string.replace).uppercase(),
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
                    value = replaceText,
                    onValueChange = {
                        replaceText = it
                        replace(findText, replaceText)
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
                        applyChanges(findText, replaceText)
                    }) {
                    openAlertDialog.value = false
                }
            }
        }
    }
}