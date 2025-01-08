package org.permanent.permanent.ui.settings.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.permanent.permanent.viewmodels.TwoStepVerificationViewModel

@Composable
fun TwoStepVerificationStatefulScreen(
    viewModel: TwoStepVerificationViewModel
) {
    val isTwoFAEnabled by viewModel.isTwoFAEnabled.collectAsState()

    if (isTwoFAEnabled) {
        // Show the enabled version of the screen
        TwoStepVerificationEnabledScreen(
            viewModel,
            onChangeVerificationMethodClick = {
            }
        )
    } else {
        // Show the disabled version of the screen
        TwoStepVerificationDisabledScreen(
            onAddTwoStepVerificationClick = {
            }
        )
    }
}
