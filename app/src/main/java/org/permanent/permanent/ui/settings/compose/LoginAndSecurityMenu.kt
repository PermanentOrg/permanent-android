package org.permanent.permanent.ui.settings.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.MenuItem
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@Composable
fun LoginAndSecurityMenu(
    viewModel: LoginAndSecurityViewModel,
    onChangePasswordClick: () -> Unit,
    onTwoStepVerificationClick: () -> Unit
) {
    val isFingerprintEnabled by viewModel.isBiometricsEnabled.collectAsState()
    val isTwoFAEnabled by viewModel.isTwoFAEnabled.collectAsState()

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

            Divider()

            MenuItem(
                iconResource = painterResource(id = R.drawable.ic_lock_primary),
                title = stringResource(R.string.two_step_verification),
                subtitle = stringResource(R.string.two_step_verification_description),
                showOffLabel = !isTwoFAEnabled,
                showOnLabel = isTwoFAEnabled,
                showArrow = true,
            ) { onTwoStepVerificationClick() }

            Divider()

            MenuItem(iconResource = painterResource(id = R.drawable.ic_fingerprint_primary),
                iconSize = 24.dp,
                title = stringResource(R.string.sign_in_with_fingerprint),
                subtitle = stringResource(R.string.sign_in_with_fingerprint_description),
                showSwitch = true,
                switchChecked = isFingerprintEnabled,
                onSwitchCheckedChange = { isChecked ->
                    viewModel.updateBiometricsEnabled(isChecked)
                }) {}
        }
    }
}