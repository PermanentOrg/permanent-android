@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.settings.compose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.permanent.permanent.viewmodels.TwoStepVerificationViewModel

@Composable
fun TwoStepVerificationStatefulScreen(
    viewModel: TwoStepVerificationViewModel
) {
    val isTwoFAEnabled by viewModel.isTwoFAEnabled.collectAsState()
    val scope = rememberCoroutineScope()

    // State to control the visibility of the bottom sheet dialog
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Ensures it opens fully
    )
    var onPasswordConfirmed by remember { mutableStateOf({}) }
    var showBottomSheet by remember { mutableStateOf(false) } // Controls visibility

    if (showBottomSheet) {
        PasswordConfirmationBottomSheet(
            viewModel,
            sheetState = bottomSheetState,
            onDismiss = {
                scope.launch {
                    bottomSheetState.hide() // Hides the sheet with animation
                    showBottomSheet = false // Ensure it can be reopened later
                }
            },
            onConfirm = { password ->
                viewModel.verifyPassword(password) { errorMessage ->
                    if (errorMessage == null) {
                        onPasswordConfirmed()
                    }
                }
            }
        )
    }

    if (isTwoFAEnabled) {
        TwoStepVerificationEnabledScreen(viewModel, onChangeVerificationMethodClick = {
            onPasswordConfirmed = {
                // Navigate to change verification method screen
            }
            scope.launch {
                if (!bottomSheetState.isVisible) {
                    showBottomSheet = true
                    bottomSheetState.show() // Animates the sheet to visible
                }
            }
        })
    } else {
        TwoStepVerificationDisabledScreen(onAddTwoStepVerificationClick = {
            onPasswordConfirmed = {
                // Navigate to enable 2FA screen
            }
            scope.launch {
                if (!bottomSheetState.isVisible) {
                    showBottomSheet = true
                    bottomSheetState.show() // Animates the sheet to visible
                }
            }
        })
    }
}