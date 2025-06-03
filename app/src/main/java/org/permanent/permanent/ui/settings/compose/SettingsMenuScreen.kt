@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.SettingsMenuItem
import org.permanent.permanent.ui.composeComponents.StorageCard
import org.permanent.permanent.ui.composeComponents.StorageCardStyle
import org.permanent.permanent.viewmodels.SettingsMenuViewModel


@Composable
fun SettingsMenuScreen(
    viewModel: SettingsMenuViewModel,
    onDismiss: () -> Unit,
    onFinishAccountSetupClick: () -> Unit,
    onAccountClick: () -> Unit,
    onStorageClick: () -> Unit,
    onMyArchivesClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onActivityFeedClick: () -> Unit,
    onLoginAndSecurityClick: () -> Unit,
    onLegacyPlanningClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val configuration = LocalConfiguration.current

    val archiveThumb by viewModel.getArchiveThumb().observeAsState("")
    val accountName by viewModel.getAccountName().observeAsState("")
    val accountEmail by viewModel.getAccountEmail().observeAsState("")

    val spaceTotalBytes by viewModel.getSpaceTotal().observeAsState(initial = 0L)
    val spaceUsedBytes by viewModel.getSpaceUsed().observeAsState(initial = 0L)
    val spaceUsedPercentage by viewModel.getSpaceUsedPercentage().observeAsState(initial = 0)

    val isTwoFAEnabled by viewModel.isTwoFAEnabled().observeAsState(initial = false)
    val checklist = viewModel.checklistItems.value

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { sheetState.hide() }
            onDismiss()
        },
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier
            .fillMaxHeight(0.95f)
            .then(
                if (viewModel.isTablet()) Modifier.width(configuration.screenWidthDp.dp * 0.5f) // 50% of the screen width for tablets
                else Modifier.fillMaxWidth() // Full width for phones
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.blue25))
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Header(
                archiveThumbURL = archiveThumb,
                accountName = accountName,
                accountEmail = accountEmail,
            ) { onDismiss() }

            if (checklist.any { !it.completed }) {
                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = colorResource(R.color.blue50))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                        .clickable { onFinishAccountSetupClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        painter = painterResource(R.drawable.ic_list_blue),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.finish_account_setup),
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.blue900)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 24.dp)
                    .background(Color.White),
                verticalArrangement = Arrangement.Top
            ) {

                Spacer(modifier = Modifier.padding(top = 8.dp))

                StorageCard(
                    spaceUsedBytes,
                    spaceTotalBytes,
                    spaceUsedPercentage,
                    StorageCardStyle.LIGHT
                )

                SettingsMenuItem(
                    painterResource(id = R.drawable.ic_account_circle_white),
                    stringResource(R.string.account),
                ) { onAccountClick() }

                SettingsMenuItem(
                    painterResource(id = R.drawable.ic_storage_primary),
                    stringResource(R.string.storage),
                ) { onStorageClick() }

                SettingsMenuItem(
                    painterResource(id = R.drawable.ic_archives_blue),
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
                    stringResource(R.string.login_and_security),
                    showWarning = !isTwoFAEnabled,
                ) { onLoginAndSecurityClick() }

                SettingsMenuItem(
                    painterResource(id = R.drawable.ic_legacy_plannning_empty_primary),
                    stringResource(R.string.legacy_planning),
                ) { onLegacyPlanningClick() }

                Spacer(modifier = Modifier.weight(1.0f))

                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                SettingsMenuItem(
                    painterResource(id = R.drawable.ic_sign_out_red),
                    stringResource(R.string.sign_out),
                    colorResource(R.color.error500),
                ) { onSignOutClick() }

                Spacer(modifier = Modifier.height(24.dp))
            }
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
            modifier = Modifier.weight(1.0f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            if (accountName != null) {
                Text(
                    text = accountName,
                    color = colorResource(R.color.blue900),
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (accountEmail != null) {
                Text(
                    text = accountEmail,
                    color = colorResource(R.color.blue900),
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.ic_close_middle_grey),
            contentDescription = "Plus",
            colorFilter = ColorFilter.tint(colorResource(R.color.blue900)),
            modifier = Modifier
                .size(22.dp)
                .clickable { onCloseClick() },
        )
    }
}