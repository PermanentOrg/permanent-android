@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.CustomSnackbar
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoStepModalContainer(
    viewModel: LoginAndSecurityViewModel,
    sheetState: SheetState,
    pagerState: PagerState,
    onDismiss: () -> Unit
) {
    val isTablet = viewModel.isTablet()

    if (isTablet) {
        // Show Dialog on Tablets
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.95f)
            ) {
                PagerContent(viewModel, pagerState, onDismiss)
            }
        }
    } else {
        // Show Bottom Sheet on Mobile
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = null,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
        ) {
            PagerContent(viewModel, pagerState, onDismiss)
        }
    }
}

@Composable
fun PagerContent(
    viewModel: LoginAndSecurityViewModel, pagerState: PagerState, onDismiss: () -> Unit
) {
    val isBusyState by viewModel.isBusyState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().imePadding()
    ) {
        HorizontalPager(
            state = pagerState, beyondBoundsPageCount = 3, userScrollEnabled = false, modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                TwoStepVerificationPage.PASSWORD_CONFIRMATION.value -> {
                    PasswordConfirmationPage(onDismiss, onConfirm = { password ->
                        viewModel.verifyPassword(password) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(TwoStepVerificationPage.METHOD_SELECTION.value)
                            }
                        }
                    })
                }

                TwoStepVerificationPage.METHOD_SELECTION.value -> {
                    MethodChoosingPage(onDismiss, onContinue = { verificationMethod ->
                        coroutineScope.launch {
                            if (verificationMethod == VerificationMethod.EMAIL) {
                                pagerState.animateScrollToPage(TwoStepVerificationPage.EMAIL_ADDRESS_INPUT.value)
                            } else {
                                pagerState.animateScrollToPage(TwoStepVerificationPage.PHONE_NUMBER_INPUT.value)
                            }
                        }
                    })
                }

                TwoStepVerificationPage.EMAIL_ADDRESS_INPUT.value -> {
                    EmailAddressInputPage(viewModel = viewModel, onDismiss = onDismiss, onBack = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(TwoStepVerificationPage.METHOD_SELECTION.value)
                        }
                    })
                }

                TwoStepVerificationPage.PHONE_NUMBER_INPUT.value -> {
                    PhoneNumberInputPage(viewModel = viewModel, onDismiss = onDismiss, onBack = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(TwoStepVerificationPage.METHOD_SELECTION.value)
                        }
                    })
                }

                TwoStepVerificationPage.CODE_VERIFICATION.value -> {
                    CodeVerificationPage(
                        viewModel = viewModel, pagerState = pagerState,
                        onMethodDisabled = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(TwoStepVerificationPage.METHOD_SELECTION.value)
                            }
                        },
                        onDismiss = onDismiss,
                    )
                }
            }
        }

        // Full-Sheet Overlay with CircularProgressIndicator
        if (isBusyState) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(overlayColor = OverlayColor.LIGHT)
            }
        }

        CustomSnackbar(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
            isForError = snackbarType == SnackbarType.ERROR,
            message = snackbarMessage,
            buttonText = stringResource(id = R.string.ok),
            onButtonClick = {
                viewModel.clearSnackbar()
            })
    }
}

enum class TwoStepVerificationPage(val value: Int) {
    PASSWORD_CONFIRMATION(0), METHOD_SELECTION(1), EMAIL_ADDRESS_INPUT(2), PHONE_NUMBER_INPUT(3), CODE_VERIFICATION(
        4
    )
}