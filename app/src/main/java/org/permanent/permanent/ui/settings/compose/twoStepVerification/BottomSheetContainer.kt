@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.CustomSnackbar
import org.permanent.permanent.viewmodels.TwoStepVerificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContainer(
    viewModel: TwoStepVerificationViewModel, sheetState: SheetState, onDismiss: () -> Unit
) {
    val isBusyState by viewModel.isBusyState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState =
        rememberPagerState(initialPage = TwoStepVerificationPage.PASSWORD_CONFIRMATION.value,
            pageCount = { TwoStepVerificationPage.values().size })

    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.95f) // 95% of the screen height
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState, beyondBoundsPageCount = 2, userScrollEnabled = false
            ) { page ->
                when (page) {
                    TwoStepVerificationPage.PASSWORD_CONFIRMATION.value -> {
                        PasswordConfirmationPage(viewModel, onDismiss, onConfirm = { password ->
                            viewModel.verifyPassword(password) { errorMessage ->
                                if (errorMessage == null) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(TwoStepVerificationPage.METHOD_CHOOSING.value)
                                    }
                                }
                            }
                        })
                    }

                    TwoStepVerificationPage.METHOD_CHOOSING.value -> {
                        MethodChoosingPage(viewModel, onDismiss, onContinue = { verificationMethod ->
                            coroutineScope.launch {
                                if (verificationMethod == "email") {
                                    pagerState.animateScrollToPage(TwoStepVerificationPage.EMAIL_VERIFICATION.value)
                                } else {
                                    pagerState.animateScrollToPage(TwoStepVerificationPage.TEXT_VERIFICATION.value)
                                }
                            }
                        })
                    }

                    TwoStepVerificationPage.EMAIL_VERIFICATION.value -> {
                        EmailVerificationPage(viewModel, onDismiss, onContinue = {
//                            viewModel.setVerificationMethod(it)
//                            onMethodChosen()
                        })
                    }

                    TwoStepVerificationPage.TEXT_VERIFICATION.value -> {
                        EmailVerificationPage(viewModel, onDismiss, onContinue = {
//                            viewModel.setVerificationMethod(it)
//                            onMethodChosen()
                        })
                    }
                }
            }

            // Full-Sheet Overlay with CircularProgressIndicator
            if (isBusyState) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }

            CustomSnackbar(modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
                message = snackbarMessage,
                buttonText = stringResource(id = R.string.ok),
                onButtonClick = {
                    viewModel.clearSnackbar()
                })
        }
    }
}

enum class TwoStepVerificationPage(val value: Int) {
    PASSWORD_CONFIRMATION(0), METHOD_CHOOSING(1), EMAIL_VERIFICATION(2), TEXT_VERIFICATION(3), CODE_VERIFICATION(
        4
    )
}