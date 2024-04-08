package org.permanent.permanent.ui.storage.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.FeedbackSnackbar
import org.permanent.permanent.ui.composeComponents.StorageCard
import org.permanent.permanent.ui.composeComponents.StorageMenuItem
import org.permanent.permanent.viewmodels.StorageMenuViewModel

@Composable
fun StorageMenuScreen(
    viewModel: StorageMenuViewModel,
    onAddStorageClick: () -> Unit,
    onGiftStorageClick: () -> Unit,
    onRedeemCodeClick: () -> Unit
) {
    val context = LocalContext.current

    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))

    val spaceTotalBytes by viewModel.getSpaceTotal().observeAsState(initial = 0L)
    val spaceUsedBytes by viewModel.getSpaceUsed().observeAsState(initial = 0L)
    val spaceUsedPercentage by viewModel.getSpaceUsedPercentage().observeAsState(initial = 0)

    val showSuccess by viewModel.showSuccess.observeAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlueColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            StorageCard(
                spaceUsedBytes,
                spaceTotalBytes,
                spaceUsedPercentage,
            )

            StorageMenuItem(
                painterResource(id = R.drawable.ic_plus_primary),
                stringResource(R.string.add_storage),
                stringResource(R.string.add_storage_description),
                showArrow = true,
            ) { onAddStorageClick() }

            Divider()

            StorageMenuItem(
                painterResource(id = R.drawable.ic_gift_primary),
                stringResource(R.string.gift_storage),
                stringResource(R.string.gift_storage_description),
                showArrow = true,
            ) { onGiftStorageClick() }

            Divider()

            StorageMenuItem(
                painterResource(id = R.drawable.ic_redeem_primary),
                stringResource(R.string.redeem_code),
                stringResource(R.string.redeem_code_description),
                showNewLabel = true,
                showArrow = true,
            ) { onRedeemCodeClick() }
        }

        LaunchedEffect(showSuccess) {
            showSuccess?.let { _ ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("", duration = SnackbarDuration.Indefinite)
                }
            }
        }

        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomStart),
            hostState = snackbarHostState
        ) { snackbarData: SnackbarData ->
            showSuccess?.let {
                FeedbackSnackbar(
                    title = stringResource(R.string.gift_code_redeemed),
                    subtitle = it,
                    isForSuccess = true
                ) {
                    viewModel.showSuccess.value = null
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
}