@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.CustomSnackbar
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@Composable
fun TwoStepStatefulScreen(
    viewModel: LoginAndSecurityViewModel
) {
    val isTwoFAEnabled by viewModel.isTwoFAEnabled.collectAsState()
    val isBusyState by viewModel.isBusyState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()
    val pagerState =
        rememberPagerState(initialPage = TwoStepVerificationPage.PASSWORD_CONFIRMATION.value,
            pageCount = { TwoStepVerificationPage.values().size })
    val coroutineScope = rememberCoroutineScope()

    // State to control the visibility of the bottom sheet dialog
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Ensures it opens fully
    )
    var showBottomSheet by remember { mutableStateOf(false) } // Controls visibility

    if (showBottomSheet) {
        BottomSheetContainer(viewModel,
            sheetState = bottomSheetState,
            pagerState = pagerState,
            onDismiss = {
                coroutineScope.launch {
                    bottomSheetState.hide() // Hides the sheet with animation
                    showBottomSheet = false // Ensure it can be reopened later
                }
            })
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isTwoFAEnabled) {
            TwoStepEnabledScreen(viewModel, onChangeVerificationMethodClick = {
                coroutineScope.launch {
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = true
                        bottomSheetState.show() // Animates the sheet to visible
                    }
                }
            }, onDeleteVerificationMethodClick = {
                viewModel.clearSnackbar()
                coroutineScope.launch {
                    showBottomSheet = true
                    bottomSheetState.show() // Animates the sheet to visible
                }
                coroutineScope.launch { // This needs to be in a separate coroutine scope or it doesn't work
                    pagerState.scrollToPage(TwoStepVerificationPage.CODE_VERIFICATION.value)
                }
            })
        } else {
            TwoStepDisabledScreen(onAddTwoStepVerificationClick = {
                viewModel.clearSnackbar()
                coroutineScope.launch {
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = true
                        bottomSheetState.show() // Animates the sheet to visible
                    }
                }
                coroutineScope.launch { // This needs to be in a separate coroutine scope or it doesn't work
                    pagerState.scrollToPage(TwoStepVerificationPage.PASSWORD_CONFIRMATION.value)
                }
            })
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