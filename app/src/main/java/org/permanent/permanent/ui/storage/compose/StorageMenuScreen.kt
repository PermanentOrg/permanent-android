package org.permanent.permanent.ui.storage.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.MenuItem
import org.permanent.permanent.ui.composeComponents.StorageCard
import org.permanent.permanent.viewmodels.StorageMenuViewModel

@Composable
fun StorageMenuScreen(
    viewModel: StorageMenuViewModel,
    onAddStorageClick: () -> Unit,
    onGiftStorageClick: () -> Unit,
    onRedeemCodeClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))

    val spaceTotalBytes by viewModel.getSpaceTotal().observeAsState(initial = 0L)
    val spaceUsedBytes by viewModel.getSpaceUsed().observeAsState(initial = 0L)
    val spaceUsedPercentage by viewModel.getSpaceUsedPercentage().observeAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlueColor)
            .padding(vertical = 24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        StorageCard(
            spaceUsedBytes,
            spaceTotalBytes,
            spaceUsedPercentage,
        )

        MenuItem(
            painterResource(id = R.drawable.ic_plus_primary),
            stringResource(R.string.add_storage),
            stringResource(R.string.add_storage_description)
        ) { onAddStorageClick() }

        Divider()

        MenuItem(
            painterResource(id = R.drawable.ic_gift_primary),
            stringResource(R.string.gift_storage),
            stringResource(R.string.gift_storage_description)
        ) { onGiftStorageClick() }

        Divider()

        MenuItem(
            painterResource(id = R.drawable.ic_redeem_primary),
            stringResource(R.string.redeem_code),
            stringResource(R.string.redeem_code_description)
        ) { onRedeemCodeClick() }
    }
}