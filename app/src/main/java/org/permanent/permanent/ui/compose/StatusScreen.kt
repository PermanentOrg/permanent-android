package org.permanent.permanent.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.viewmodels.LegacyStatusViewModel

@Composable
fun StatusScreen(viewModel: LegacyStatusViewModel,
                 navigateToLegacyContactScreen: () -> Unit,
                 navigateToArchiveStewardScreen: (archive: Archive) -> Unit) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))

    val legacyStewards = viewModel.getOnLegacyContactReady().observeAsState()
    val allArchives = viewModel.getOnAllArchivesReady().observeAsState()

    Column(
        modifier = Modifier
            .background(color = primaryColor)
            .verticalScroll(rememberScrollState())
    ) {
        legacyStewards.value?.forEach {
            AccountCard(steward = it, navigateToLegacyContactScreen)
        }

        allArchives.value?.forEach {
            it.second?.let { _ ->
                ArchiveCardCompleted(info = it, navigateToArchiveStewardScreen)
            } ?: run {
                ArchiveCard(info = it, navigateToArchiveStewardScreen)
            }
        }
    }
}

@Composable
fun AccountCard(steward: LegacyContact,
                navigateToLegacyContactScreen: () -> Unit) {
    val context = LocalContext.current
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val darkBlueColor = Color(ContextCompat.getColor(context, R.color.darkBlue))
    val whiteSuperTransparentColor = Color(ContextCompat.getColor(context, R.color.whiteSuperTransparent))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val stewardName = steward.name

    Card(
        modifier = Modifier
            .padding(24.dp),
        border = BorderStroke(1.dp, whiteSuperTransparentColor),
        colors = CardDefaults.cardColors(
            containerColor = darkBlueColor)
    ){
        Column(
            modifier = Modifier
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row {
                Image(
                    painter = painterResource(id = R.drawable.ic_legacy_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(onClick = {
                    navigateToLegacyContactScreen()
                }) {
                    Text("Edit Plan",
                        color = whiteColor,
                        fontFamily = regularFont,
                        fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_edit_primary),
                        contentDescription = "Person",
                        colorFilter = ColorFilter.tint(whiteColor),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Divider()
            Text(
                text = stringResource(id = R.string.in_the_event),
                color = whiteColor,
                fontFamily = regularFont,
                fontSize = 13.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_account_primary),
                    contentDescription = "Person",
                    modifier = Modifier.size(24.dp)
                )

                stewardName?.let {
                    Text(
                        text = it,
                        color = whiteColor,
                        fontFamily = semiboldFont,
                        fontSize = 13.sp
                    )
                }

            }
        }
    }
}

@Composable
fun ArchiveCard(info: Pair<Archive, ArchiveSteward?>,
                navigateToArchiveStewardScreen: (archive: Archive) -> Unit) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val whiteSuperTransparentColor = Color(ContextCompat.getColor(context, R.color.whiteSuperTransparent))
    val mardiGrasColor = Color(ContextCompat.getColor(context, R.color.mardiGras))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val archiveName = if(info.first.fullName != null) info.first.fullName else ""
    val accessRoleText = if(info.first.accessRole?.toTitleCase() != null) info.first.accessRole?.toTitleCase() else ""
    val cardColor = Color(ContextCompat.getColor(context, R.color.white))
    val borderWidth = 0.dp

    Card(
        modifier = Modifier
            .padding(24.dp),
        border = BorderStroke(borderWidth, whiteSuperTransparentColor),
        colors = CardDefaults.cardColors(
            containerColor = cardColor)

    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AsyncImage(
                model = info.first.thumbURL200,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            if (archiveName != null) {
                Text(
                    text = archiveName,
                    color = primaryColor,
                    fontFamily = semiboldFont,
                    fontSize = 15.sp
                )
            }

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        mardiGrasColor.copy(alpha = 0.2f)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 2.dp
                    )
            ) {
                if (accessRoleText != null) {
                    Text(text = accessRoleText.uppercase(),
                        color = primaryColor,
                        fontFamily = regularFont,
                        fontSize = 8.sp)
                }
            }
            Divider()
            TextButton(onClick = { navigateToArchiveStewardScreen(info.first)},
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    stringResource(id = R.string.create_legacy),
                    color = primaryColor,
                    fontFamily = semiboldFont,
                    fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1.0f))
                Image(
                    painter = painterResource(id = R.drawable.ic_drop_down_white),
                    contentDescription = "Person",
                    colorFilter = ColorFilter.tint(primaryColor),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-90.0f)
                )
            }

        }
    }
}

@Composable
fun ArchiveCardCompleted(info: Pair<Archive, ArchiveSteward?>,
                         navigateToArchiveStewardScreen: (archive: Archive) -> Unit) {
    val context = LocalContext.current
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val whiteSuperTransparentColor = Color(ContextCompat.getColor(context, R.color.whiteSuperTransparent))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val archiveName = info.first.fullName
    val cardColor = Color(ContextCompat.getColor(context, R.color.darkBlue))
    val borderWidth = 1.dp
    val textColor = whiteColor
    val stewardName = info.second?.steward?.name

    Card(
        modifier = Modifier
            .padding(24.dp),
        border = BorderStroke(borderWidth, whiteSuperTransparentColor),
        colors = CardDefaults.cardColors(
            containerColor = cardColor)

    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = info.first.thumbURL200,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.weight(1.0f))

                TextButton(onClick = { navigateToArchiveStewardScreen(info.first) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Edit Plan",
                        color = whiteColor,
                        fontFamily = regularFont,
                        fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_edit_primary),
                        contentDescription = "Person",
                        colorFilter = ColorFilter.tint(whiteColor),
                        modifier = Modifier.size(16.dp)
                    )
                }

            }

            Divider()

            archiveName?.let {
                Text(
                    text = it,
                    color = textColor,
                    fontFamily = semiboldFont,
                    fontSize = 15.sp
                )
            }

            Text(
                text = stringResource(id = R.string.your_archive_will),
                color = whiteColor.copy(alpha = 0.85f),
                fontFamily = regularFont,
                fontSize = 13.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_account_primary),
                    contentDescription = "Person",
                    modifier = Modifier.size(24.dp)
                )

                stewardName?.let {
                    Text(
                        text = it,
                        color = whiteColor,
                        fontFamily = semiboldFont,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}