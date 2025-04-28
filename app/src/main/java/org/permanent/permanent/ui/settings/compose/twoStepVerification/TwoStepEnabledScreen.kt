@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.network.models.TwoFAVO
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.ConfirmationBottomSheet
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@Composable
fun TwoStepEnabledScreen(
    viewModel: LoginAndSecurityViewModel,
    onChangeVerificationMethodClick: () -> Unit,
    onDeleteVerificationMethodClick: () -> Unit
) {
    if (viewModel.isTablet()) {
        TabletBody(viewModel, onChangeVerificationMethodClick, onDeleteVerificationMethodClick)
    } else {
        PhoneBody(viewModel, onChangeVerificationMethodClick, onDeleteVerificationMethodClick)
    }
}

@Composable
private fun TabletBody(
    viewModel: LoginAndSecurityViewModel,
    onChangeVerificationMethodClick: () -> Unit,
    onDeleteVerificationMethodClick: () -> Unit
) {
    val twoFAList by viewModel.twoFAList.collectAsState()
    var confirmationSheetMessageRes by remember { mutableStateOf<Int?>(null) }
    var confirmationSheetBoldTextRes by remember { mutableStateOf<Int?>(null) }
    var confirmationSheetButtonTextRes by remember { mutableIntStateOf(R.string.delete) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItemIndex by remember { mutableStateOf<Int?>(null) }

    val confirmationSheetMessage = confirmationSheetMessageRes?.let { stringResource(it) } ?: ""
    val confirmationSheetBoldText = confirmationSheetBoldTextRes?.let { stringResource(it) } ?: ""

    if (showBottomSheet) {
        ConfirmationBottomSheet(message = confirmationSheetMessage,
            boldText = confirmationSheetBoldText,
            confirmationButtonText = stringResource(id = confirmationSheetButtonTextRes),
            onConfirm = {
                selectedItemIndex?.let { index ->
                    val twoFAVO = twoFAList.toMutableList()[index]
                    viewModel.updateTwoFAMethodToDisable(twoFAVO)
                    showBottomSheet = false
                    if (confirmationSheetButtonTextRes == R.string.delete) {
                        viewModel.setIsChangeVerificationMethod(false)
                        onDeleteVerificationMethodClick()
                    } else {
                        viewModel.setIsChangeVerificationMethod(true)
                        onChangeVerificationMethodClick()
                    }
                }
            },
            onDismiss = { showBottomSheet = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.blue25))
            .padding(horizontal = 128.dp, vertical = 64.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Title Text
            Text(
                text = stringResource(id = R.string.two_step_verification),
                color = colorResource(id = R.color.colorPrimary),
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Top banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.lightGreen),
                        shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_lock_closed_green),
                    contentDescription = "Enabled Lock",
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(id = R.string.two_step_verification_is))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(" " + stringResource(id = R.string.enabled) + ".")
                        }
                    },
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = colorResource(id = R.color.textGreen),
                    fontFamily = FontFamily(Font(R.font.usual_regular))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description Text
            Text(
                text = stringResource(id = R.string.two_step_verification_description),
                color = colorResource(id = R.color.colorPrimary),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Verification Methods List
            if (twoFAList.isNotEmpty()) {
                twoFAList.forEachIndexed { index, item ->
                    VerificationItemCard(
                        item = item,
                        onRemove = {
                            selectedItemIndex = index
                            confirmationSheetMessageRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.remove_sms_verification_method
                                } else {
                                    R.string.remove_email_verification_method
                                }
                            confirmationSheetBoldTextRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.text_message_sms
                                } else {
                                    R.string.email
                                }
                            confirmationSheetButtonTextRes = R.string.delete
                            showBottomSheet = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (twoFAList.size == 1) {
                // Change Verification Method Button
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CenteredTextAndIconButton(buttonColor = ButtonColor.DARK,
                        text = stringResource(id = R.string.change_verification_method),
                        icon = null,
                        onButtonClick = {
                            selectedItemIndex = 0
                            val item = twoFAList[0]
                            confirmationSheetMessageRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.change_sms_verification_method
                                } else {
                                    R.string.change_email_verification_method
                                }
                            confirmationSheetBoldTextRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.sms_verification_method
                                } else {
                                    R.string.email_verification_method
                                }
                            confirmationSheetButtonTextRes = R.string.button_continue
                            showBottomSheet = true
                        })
                }
            }
        }

        // Bottom Section
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = colorResource(id = R.color.dividerBlue)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.two_step_verification_help_text),
                color = colorResource(id = R.color.blue),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PhoneBody(
    viewModel: LoginAndSecurityViewModel,
    onChangeVerificationMethodClick: () -> Unit,
    onDeleteVerificationMethodClick: () -> Unit
) {
    val twoFAList by viewModel.twoFAList.collectAsState()
    var confirmationSheetMessageRes by remember { mutableStateOf<Int?>(null) }
    var confirmationSheetBoldTextRes by remember { mutableStateOf<Int?>(null) }
    var confirmationSheetButtonTextRes by remember { mutableIntStateOf(R.string.delete) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItemIndex by remember { mutableStateOf<Int?>(null) }

    val confirmationSheetMessage = confirmationSheetMessageRes?.let { stringResource(it) } ?: ""
    val confirmationSheetBoldText = confirmationSheetBoldTextRes?.let { stringResource(it) } ?: ""

    if (showBottomSheet) {
        ConfirmationBottomSheet(message = confirmationSheetMessage,
            boldText = confirmationSheetBoldText,
            confirmationButtonText = stringResource(id = confirmationSheetButtonTextRes),
            onConfirm = {
                selectedItemIndex?.let { index ->
                    val twoFAVO = twoFAList.toMutableList()[index]
                    viewModel.updateTwoFAMethodToDisable(twoFAVO)
                    showBottomSheet = false
                    if (confirmationSheetButtonTextRes == R.string.delete) {
                        viewModel.setIsChangeVerificationMethod(false)
                        onDeleteVerificationMethodClick()
                    } else {
                        viewModel.setIsChangeVerificationMethod(true)
                        onChangeVerificationMethodClick()
                    }
                }
            },
            onDismiss = { showBottomSheet = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.blue25)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Snackbar-style banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.lightGreen))
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_lock_closed_green),
                    contentDescription = "Enabled Lock",
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(id = R.string.two_step_verification_is))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(" " + stringResource(id = R.string.enabled) + ".")
                        }
                    },
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = colorResource(id = R.color.textGreen),
                    fontFamily = FontFamily(Font(R.font.usual_regular))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description Text
            Text(
                text = stringResource(id = R.string.two_step_verification_description),
                color = colorResource(id = R.color.colorPrimary),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Verification Methods List
            if (twoFAList.isNotEmpty()) {
                twoFAList.forEachIndexed { index, item ->
                    VerificationItemCard(
                        item = item,
                        onRemove = {
                            selectedItemIndex = index
                            confirmationSheetMessageRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.remove_sms_verification_method
                                } else {
                                    R.string.remove_email_verification_method
                                }
                            confirmationSheetBoldTextRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.text_message_sms
                                } else {
                                    R.string.email
                                }
                            confirmationSheetButtonTextRes = R.string.delete
                            showBottomSheet = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (twoFAList.size == 1) {
                // Change Verification Method Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    CenteredTextAndIconButton(buttonColor = ButtonColor.DARK,
                        text = stringResource(id = R.string.change_verification_method),
                        icon = null,
                        onButtonClick = {
                            selectedItemIndex = 0
                            val item = twoFAList[0]
                            confirmationSheetMessageRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.change_sms_verification_method
                                } else {
                                    R.string.change_email_verification_method
                                }
                            confirmationSheetBoldTextRes =
                                if (item.method == VerificationMethod.SMS.name.lowercase()) {
                                    R.string.sms_verification_method
                                } else {
                                    R.string.email_verification_method
                                }
                            confirmationSheetButtonTextRes = R.string.button_continue
                            showBottomSheet = true
                        })
                }
            }
        }

        // Bottom Section
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = colorResource(id = R.color.dividerBlue)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.two_step_verification_help_text),
                color = colorResource(id = R.color.blue),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun VerificationItemCard(
    item: TwoFAVO, onRemove: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(
            width = 1.dp,
            color = colorResource(R.color.blue50),
            shape = RoundedCornerShape(size = 12.dp)
        ), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 24.dp, start = 24.dp, end = 10.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Content Column
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(if (item.method == VerificationMethod.SMS.name.lowercase()) R.string.text_message else R.string.email),
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_medium)),
                        color = colorResource(R.color.blue900)
                    )
                    if (item.method == VerificationMethod.SMS.name.lowercase()) { // We show "default" label if it's sms
                        Spacer(modifier = Modifier.width(8.dp)) // Small spacing between title and DEFAULT
                        Text(
                            text = stringResource(R.string.default_label).uppercase(),
                            color = colorResource(R.color.blue900),
                            fontSize = 10.sp,
                            lineHeight = 24.sp,
                            letterSpacing = 1.6.sp,
                            fontFamily = FontFamily(Font(R.font.usual_medium)),
                            modifier = Modifier
                                .background(
                                    color = colorResource(R.color.blue25),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Value text
                item.value?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.blue600)
                    )
                }
            }

            // Align IconButton at the top
            IconButton(
                onClick = onRemove, modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_red),
                    contentDescription = "Remove",
                    tint = colorResource(R.color.error500)
                )
            }
        }
    }
}