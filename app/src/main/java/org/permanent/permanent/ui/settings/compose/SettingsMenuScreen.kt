package org.permanent.permanent.ui.settings.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.SettingsMenuItem
import org.permanent.permanent.ui.composeComponents.StorageCard
import org.permanent.permanent.viewmodels.SettingsMenuViewModel

@Composable
fun SettingsMenuScreen(
    viewModel: SettingsMenuViewModel,
    onCloseScreenClick: () -> Unit,
    onAccountClick: () -> Unit,
    onStorageClick: () -> Unit,
    onMyArchivesClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onActivityFeedClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onLegacyPlanningClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val context = LocalContext.current

    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val error500Color = Color(ContextCompat.getColor(context, R.color.error500))

    val archiveThumb by viewModel.getArchiveThumb().observeAsState("")
    val accountName by viewModel.getAccountName().observeAsState("")
    val accountEmail by viewModel.getAccountEmail().observeAsState("")

    val spaceTotalBytes by viewModel.getSpaceTotal().observeAsState(initial = 0L)
    val spaceUsedBytes by viewModel.getSpaceUsed().observeAsState(initial = 0L)
    val spaceUsedPercentage by viewModel.getSpaceUsedPercentage().observeAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlueColor)
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Header(
            archiveThumbURL = archiveThumb,
            accountName = accountName,
            accountEmail = accountEmail,
        ) { onCloseScreenClick() }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 24.dp)
                .background(whiteColor),
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.padding(top = 8.dp))

            StorageCard(
                spaceUsedBytes,
                spaceTotalBytes,
                spaceUsedPercentage,
                true
            )

            SettingsMenuItem(
                painterResource(id = R.drawable.ic_account_cercle_primary),
                stringResource(R.string.account),
            ) { onAccountClick() }

            SettingsMenuItem(
                painterResource(id = R.drawable.ic_storage_primary),
                stringResource(R.string.storage),
            ) { onStorageClick() }

            SettingsMenuItem(
                painterResource(id = R.drawable.ic_archives_primary),
                stringResource(R.string.my_archives),
            ) { onMyArchivesClick() }

            SettingsMenuItem(
                painterResource(id = R.drawable.ic_invitations_primary),
                stringResource(R.string.invitations),
            ) { onInvitationsClick() }

            SettingsMenuItem(
                painterResource(id = R.drawable.ic_activity_feed_primary),
                stringResource(R.string.activity_feed),
            ) { onActivityFeedClick() }

            SettingsMenuItem(
                painterResource(id = R.drawable.ic_security_primary),
                stringResource(R.string.security),
            ) { onSecurityClick() }

            SettingsMenuItem(
                painterResource(id = R.drawable.ice_legacy_plannning_empty_primary),
                stringResource(R.string.legacy_planning),
            ) { onLegacyPlanningClick() }

            Spacer(modifier = Modifier.weight(1.0f))

            Divider(modifier = Modifier.padding(bottom = 16.dp))

            SettingsMenuItem(
                painterResource(id = R.drawable.ic_sign_out_red),
                stringResource(R.string.sign_out),
                error500Color,
            ) { onSignOutClick() }
        }
    }
}

@Composable
private fun Header(
    archiveThumbURL: String?,
    accountName: String?,
    accountEmail: String?,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    val blue900Color = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = archiveThumbURL,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier.weight(1.0f, fill = false),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            if (accountName != null) {
                Text(
                    text = accountName,
                    color = blue900Color,
                    fontFamily = semiboldFont,
                    fontSize = 16.sp
                )
            }

            if (accountEmail != null) {
                Text(
                    text = accountEmail,
                    color = blue900Color,
                    fontFamily = regularFont,
                    fontSize = 14.sp
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.ic_close_middle_grey),
            contentDescription = "Plus",
            colorFilter = ColorFilter.tint(blue900Color),
            modifier = Modifier
                .size(22.dp)
                .clickable { onCloseClick() },
        )
    }
}