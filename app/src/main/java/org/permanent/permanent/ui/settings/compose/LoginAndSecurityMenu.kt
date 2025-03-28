package org.permanent.permanent.ui.settings.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.MenuItem
import org.permanent.permanent.ui.settings.compose.twoStepVerification.TwoStepStatefulScreen
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@Composable
fun LoginAndSecurityMenu(
    viewModel: LoginAndSecurityViewModel,
    onChangePasswordClick: () -> Unit,
    onTwoStepVerificationClick: () -> Unit
) {
    val isTablet = viewModel.isTablet()
    val isTwoFAEnabled by viewModel.isTwoFAEnabled.collectAsState()
    val isFingerprintEnabled by viewModel.isBiometricsEnabled.collectAsState()

    if (isTablet) {
        LoginAndSecurityTabletMenu(
            viewModel = viewModel,
            isTwoFAEnabled = isTwoFAEnabled,
            isFingerprintEnabled = isFingerprintEnabled,
            onChangePasswordClick = onChangePasswordClick,
        )
    } else {
        LoginAndSecurityPhoneMenu(
            viewModel = viewModel,
            isTwoFAEnabled = isTwoFAEnabled,
            isFingerprintEnabled = isFingerprintEnabled,
            onChangePasswordClick = onChangePasswordClick,
            onTwoStepVerificationClick = onTwoStepVerificationClick
        )
    }
}

@Composable
fun LoginAndSecurityTabletMenu(
    viewModel: LoginAndSecurityViewModel,
    isTwoFAEnabled: Boolean,
    isFingerprintEnabled: Boolean,
    onChangePasswordClick: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<SelectedScreen?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Menu Section - Occupies 1/3 of the screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(colorResource(id = R.color.white))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
            ) {
                MenuItem(
                    iconResource = painterResource(id = R.drawable.ic_key_white),
                    title = stringResource(R.string.change_password),
                    subtitle = stringResource(R.string.change_password_description),
                    showArrow = true,
                    isSelected = selectedScreen == SelectedScreen.ChangePassword
                ) { selectedScreen = SelectedScreen.ChangePassword }

                HorizontalDivider()

                MenuItem(
                    iconResource = painterResource(id = R.drawable.ic_lock_primary),
                    title = stringResource(R.string.two_step_verification),
                    subtitle = stringResource(R.string.two_step_verification_description),
                    showOffLabel = !isTwoFAEnabled,
                    showOnLabel = isTwoFAEnabled,
                    showArrow = true,
                    isSelected = selectedScreen == SelectedScreen.TwoStepVerification
                ) { selectedScreen = SelectedScreen.TwoStepVerification }

                HorizontalDivider()

                MenuItem(iconResource = painterResource(id = R.drawable.ic_fingerprint_primary),
                    iconSize = 24.dp,
                    title = stringResource(R.string.sign_in_with_fingerprint),
                    subtitle = stringResource(R.string.sign_in_with_fingerprint_description),
                    showSwitch = true,
                    switchChecked = isFingerprintEnabled,
                    onSwitchCheckedChange = { isChecked ->
                        viewModel.updateBiometricsEnabled(isChecked)
                    },
                    onClick = { viewModel.updateBiometricsEnabled(!viewModel.isBiometricsEnabled.value) })

                HorizontalDivider()
            }
        }

        VerticalDivider()

        // Content Section - Occupies 2/3 of the screen
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            when (selectedScreen) {
                SelectedScreen.ChangePassword -> {
                    onChangePasswordClick()
                }

                SelectedScreen.TwoStepVerification -> {
                    TwoStepStatefulScreen(viewModel)
                }

                null -> DefaultInfoScreen() // Placeholder when nothing is selected
            }
        }
    }
}

// Enum to track the selected screen
enum class SelectedScreen {
    ChangePassword, TwoStepVerification
}

@Composable
fun LoginAndSecurityPhoneMenu(
    viewModel: LoginAndSecurityViewModel,
    isTwoFAEnabled: Boolean,
    isFingerprintEnabled: Boolean,
    onChangePasswordClick: () -> Unit,
    onTwoStepVerificationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.superLightBlue))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
        ) {

            MenuItem(
                iconResource = painterResource(id = R.drawable.ic_key_white),
                title = stringResource(R.string.change_password),
                subtitle = stringResource(R.string.change_password_description),
                showArrow = true,
            ) { onChangePasswordClick() }

            HorizontalDivider()

            MenuItem(
                iconResource = painterResource(id = R.drawable.ic_lock_primary),
                title = stringResource(R.string.two_step_verification),
                subtitle = stringResource(R.string.two_step_verification_description),
                showOffLabel = !isTwoFAEnabled,
                showOnLabel = isTwoFAEnabled,
                showArrow = true,
            ) { onTwoStepVerificationClick() }

            HorizontalDivider()

            MenuItem(iconResource = painterResource(id = R.drawable.ic_fingerprint_primary),
                iconSize = 24.dp,
                title = stringResource(R.string.sign_in_with_fingerprint),
                subtitle = stringResource(R.string.sign_in_with_fingerprint_description),
                showSwitch = true,
                switchChecked = isFingerprintEnabled,
                onSwitchCheckedChange = { isChecked ->
                    viewModel.updateBiometricsEnabled(isChecked)
                },
                onClick = { viewModel.updateBiometricsEnabled(!viewModel.isBiometricsEnabled.value) })

            HorizontalDivider()
        }
    }
}
