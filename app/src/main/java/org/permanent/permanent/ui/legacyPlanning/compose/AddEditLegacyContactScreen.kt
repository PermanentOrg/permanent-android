@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.legacyPlanning.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.viewmodels.AddEditLegacyEntityViewModel

@Composable
fun AddEditLegacyContactScreen(
    viewModel: AddEditLegacyEntityViewModel,
    screenTitle: String,
    title: String,
    subtitle: String,
    namePlaceholder: String,
    emailPlaceholder: String,
    note: String,
    showName: Boolean,
    showMessage: Boolean,
    onCancelBtnClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.blue25))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val elementsSpacing = 32.dp
    val superSmallTextSize = 10.sp
    val smallTextSize = 13.sp
    val titleTextSize = 15.sp

    var contactName by remember { mutableStateOf(viewModel.name ?: "") }
    var contactEmail by remember { mutableStateOf(viewModel.email ?: "") }
    var message by remember { mutableStateOf(viewModel.message ?: context.getString(R.string.steward_default_message)) }
    val errorMessage by viewModel.showError.observeAsState()

    val coroutineScope = rememberCoroutineScope()
    val snackbarEventFlow = remember { MutableSharedFlow<String>() }
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteColor)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomSheetHeader(screenTitle = screenTitle, onCancelBtnClick) {
            if (showName && contactName.isEmpty()) {
                coroutineScope.launch {
                    snackbarEventFlow.emit(context.getString(R.string.name_empty_error))
                }
            } else if (contactEmail.isEmpty()) {
                coroutineScope.launch {
                    snackbarEventFlow.emit(context.getString(R.string.email_empty_error))
                }
            } else if (!Validator.isValidEmail(context, contactEmail, null, null)) {
                coroutineScope.launch {
                    snackbarEventFlow.emit(context.getString(R.string.invalid_email_error))
                }
            } else {
                viewModel.onSaveBtnClick(
                    contactEmail,
                    contactName,
                    message
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = elementsSpacing),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = titleTextSize,
                color = primaryColor,
                fontFamily = boldFont,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = elementsSpacing)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitle,
                fontSize = smallTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(elementsSpacing))
            if (showName) {
                TextField(
                    value = contactName,
                    onValueChange = { value -> contactName = value },
                    label = { Text(text = namePlaceholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_account_empty_multicolor),
                            contentDescription = "Contact Name Icon"
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = lightBlueColor,
                        unfocusedContainerColor = lightBlueColor,
                        focusedIndicatorColor = lightBlueColor,
                        unfocusedIndicatorColor = lightBlueColor,
                        focusedLabelColor = lightGreyColor,
                        unfocusedLabelColor = lightGreyColor
                    )
                )
            }
            TextField(
                value = contactEmail,
                onValueChange = { value -> contactEmail = value },
                label = { Text(text = emailPlaceholder) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_email_multicolor),
                        contentDescription = "Contact Email Icon"
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = lightBlueColor,
                    unfocusedContainerColor = lightBlueColor,
                    focusedIndicatorColor = lightBlueColor,
                    unfocusedIndicatorColor = lightBlueColor,
                    focusedLabelColor = lightGreyColor,
                    unfocusedLabelColor = lightGreyColor
                )
            )
            if (showMessage) {
                TextField(
                    value = message,
                    onValueChange = { value -> message = value },
                    label = { Text(text = context.getString(R.string.steward_message)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_message_multicolor),
                            contentDescription = "Message Icon"
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = lightBlueColor,
                        unfocusedContainerColor = lightBlueColor,
                        focusedIndicatorColor = lightBlueColor,
                        unfocusedIndicatorColor = lightBlueColor,
                        focusedLabelColor = lightGreyColor,
                        unfocusedLabelColor = lightGreyColor
                    )
                )
            }
            Divider(modifier = Modifier.padding(vertical = elementsSpacing))
            Text(
                text = stringResource(R.string.note_title).uppercase(),
                fontSize = superSmallTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = note,
                fontSize = smallTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(elementsSpacing))

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(snackbarEventFlow) {
        snackbarEventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
private fun BottomSheetHeader(
    screenTitle: String,
    onCancelBtnClick: () -> Unit,
    onSaveBtnClick: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val subTitleTextSize = 16.sp
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onCancelBtnClick() }
        ) {
            Text(
                text = stringResource(R.string.button_cancel),
                fontSize = subTitleTextSize,
                fontFamily = regularFont,
                color = whiteColor
            )
        }
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            text = screenTitle,
            fontSize = subTitleTextSize,
            fontFamily = semiBoldFont,
            color = whiteColor
        )
        Spacer(modifier = Modifier.weight(1.0f))
        TextButton(onClick = { onSaveBtnClick() }
        ) {
            Text(
                text = stringResource(R.string.button_save),
                fontSize = subTitleTextSize,
                fontFamily = regularFont,
                color = whiteColor
            )
        }
    }
}