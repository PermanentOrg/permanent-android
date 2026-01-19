package org.permanent.permanent.ui.shareManagement.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.ConfirmationBottomSheet
import org.permanent.permanent.viewmodels.ShareManagementViewModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LinkSettingsPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val shareLink by viewModel.shareLink.collectAsState()
    val selectedGeneralAccessType by viewModel.selectedGeneralAccessType.collectAsState()
    val selectedAccessRole by viewModel.selectedAccessRole.collectAsState()
    val selectedLinkDuration by viewModel.selectedLinkDuration.collectAsState()
    val createdAtDate by viewModel.createdAtDate.collectAsState()
    var showRevokeConfirmation by remember { mutableStateOf(false) }
    val durationOptions = remember { LinkDuration.entries }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NavigationHeader(
                title = stringResource(R.string.link_settings),
                onBackBtnClick = { viewModel.onBackBtnClick(SharePage.LINK_SETTINGS) },
                onCloseClick = onClose
            )
        },
        bottomBar = {
            Column {
                // Top gradient fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White
                                )
                            )
                        )
                )

                // Buttons container
                Surface(color = Color.White, tonalElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CenteredTextAndIconButton(
                                buttonColor = ButtonColor.LIGHT_BLUE,
                                text = stringResource(id = R.string.button_cancel),
                                icon = null,
                                onButtonClick = { viewModel.onBackBtnClick(SharePage.LINK_SETTINGS) })
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            CenteredTextAndIconButton(
                                buttonColor = ButtonColor.DARK,
                                text = stringResource(id = R.string.done),
                                icon = null,
                                onButtonClick = { viewModel.onDoneBtnClick() })
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        // only this area scrolls; topBar and bottomBar remain fixed
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.blue25))
        ) {
            item {
                LinkRow(
                    shareLink = viewModel.cleanUrlRegex(shareLink),
                    onCopyClick = { viewModel.copyLinkToClipboard() }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    // General access header + row
                    Text(
                        text = stringResource(R.string.general_access).toUpperCase(Locale.current),
                        style = TextStyle(
                            fontSize = 10.sp,
                            lineHeight = 8.sp,
                            fontFamily = FontFamily(Font(R.font.usual_regular)),
                            color = colorResource(R.color.colorPrimary),
                            letterSpacing = 1.6.sp,
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onGeneralAccessClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    colorResource(R.color.success50),
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = when (selectedGeneralAccessType) {
                                        AccessType.ANYONE_CAN_VIEW -> R.drawable.ic_globe_green
                                        AccessType.RESTRICTED -> R.drawable.ic_lock_green
                                    }
                                ), contentDescription = "", tint = Color.Unspecified
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = when (selectedGeneralAccessType) {
                                AccessType.ANYONE_CAN_VIEW -> stringResource(R.string.anyone_can_view)
                                AccessType.RESTRICTED -> stringResource(R.string.restricted)
                            }, modifier = Modifier.weight(1f), style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily(Font(R.font.usual_medium)),
                                color = colorResource(R.color.colorPrimary),
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_select_light_blue),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // default access role only when restricted
                    if (selectedGeneralAccessType == AccessType.RESTRICTED) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.default_access_role).toUpperCase(Locale.current),
                            style = TextStyle(
                                fontSize = 10.sp,
                                lineHeight = 8.sp,
                                fontFamily = FontFamily(Font(R.font.usual_regular)),
                                color = colorResource(R.color.colorPrimary),
                                letterSpacing = 1.6.sp,
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onDefaultAccessRoleClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        colorResource(R.color.success50),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = when (selectedAccessRole) {
                                            AccessRole.VIEWER -> R.drawable.ic_viewer_green
                                            AccessRole.CONTRIBUTOR -> R.drawable.ic_contributor_green
                                            AccessRole.EDITOR -> R.drawable.ic_editor_green
                                            AccessRole.CURATOR -> R.drawable.ic_curator_green
                                            AccessRole.OWNER -> R.drawable.ic_owner_green
                                            AccessRole.MANAGER -> TODO()
                                        }
                                    ), contentDescription = "", tint = Color.Unspecified
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = when (selectedAccessRole) {
                                    AccessRole.VIEWER -> AccessRole.VIEWER.toTitleCase()
                                    AccessRole.CONTRIBUTOR -> AccessRole.CONTRIBUTOR.toTitleCase()
                                    AccessRole.EDITOR -> AccessRole.EDITOR.toTitleCase()
                                    AccessRole.CURATOR -> AccessRole.CURATOR.toTitleCase()
                                    AccessRole.OWNER -> AccessRole.OWNER.toTitleCase()
                                    AccessRole.MANAGER -> AccessRole.MANAGER.toTitleCase()
                                }, modifier = Modifier.weight(1f), style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                                    color = colorResource(R.color.colorPrimary),
                                )
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_select_light_blue),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(12.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Link expiration
                    Text(
                        text = stringResource(R.string.link_expiration).toUpperCase(Locale.current),
                        style = TextStyle(
                            fontSize = 10.sp,
                            lineHeight = 8.sp,
                            fontFamily = FontFamily(Font(R.font.usual_regular)),
                            color = colorResource(R.color.colorPrimary),
                            letterSpacing = 1.6.sp,
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val option1 = durationOptions[0] // ONE_DAY
                            val option2 = durationOptions[1] // ONE_MONTH

                            DurationOption(
                                icon = painterResource(id = option1.iconRes),
                                label = stringResource(option1.labelRes),
                                selected = selectedLinkDuration == option1,
                                onClick = { viewModel.onLinkDurationSelected(option1) },
                                modifier = Modifier.weight(1f)
                            )

                            DurationOption(
                                icon = painterResource(id = option2.iconRes),
                                label = stringResource(option2.labelRes),
                                selected = selectedLinkDuration == option2,
                                onClick = { viewModel.onLinkDurationSelected(option2) },
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val option3 = durationOptions[2] // ONE_YEAR
                            val option4 = durationOptions[3] // NEVER

                            DurationOption(
                                icon = painterResource(id = option3.iconRes),
                                label = stringResource(option3.labelRes),
                                selected = selectedLinkDuration == option3,
                                onClick = { viewModel.onLinkDurationSelected(option3) },
                                modifier = Modifier.weight(1f),
                            )

                            DurationOption(
                                icon = painterResource(id = option4.iconRes),
                                label = stringResource(option4.labelRes),
                                selected = selectedLinkDuration == option4,
                                onClick = { viewModel.onLinkDurationSelected(option4) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    InfoBanner(duration = selectedLinkDuration, createdAt = createdAtDate)

                    Spacer(Modifier.height(24.dp))

                    HorizontalDivider(thickness = 1.dp, color = colorResource(R.color.blue50))

                    Spacer(Modifier.height(16.dp))

                    RevokeLink(onClick = { showRevokeConfirmation = true })
                }
            }
        }
    }

    if (showRevokeConfirmation) {
        ConfirmationBottomSheet(
            message = stringResource(R.string.confirm_revoke_link_message),
            boldText = stringResource(R.string.revoke_this_share_link),
            confirmationButtonText = stringResource(id = R.string.revoke_link),
            onConfirm = {
                showRevokeConfirmation = false
                viewModel.revokeLink()
            },
            onDismiss = {
                showRevokeConfirmation = false
            })
    }
}

@Composable
fun NavigationHeader(
    title: String, onBackBtnClick: () -> Unit, onCloseClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.white))
                .padding(12.dp), contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBackBtnClick, modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back_blue),
                    contentDescription = "Back",
                    tint = Color.Unspecified,
                )
            }

            Text(
                text = title, style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                    textAlign = TextAlign.Center
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            if (onCloseClick != null) {
                IconButton(
                    onClick = onCloseClick, modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_light_blue),
                        contentDescription = "Close",
                        tint = colorResource(R.color.blue200),
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp, color = colorResource(R.color.blue100)
        )
    }
}

@Composable
fun LinkRow(
    shareLink: String, onCopyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.blue50),
                    shape = RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(R.color.white))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_link_gradient),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = shareLink, modifier = Modifier.weight(1f), style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        textAlign = TextAlign.Left,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(R.color.barneyPurple),
                                colorResource(R.color.colorAccent)
                            ), start = Offset(0f, 0f), end = Offset(300f, 300f)
                        ),
                    ), maxLines = 1, overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .clickable { onCopyClick() }
                        .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy_blue),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(R.string.copy), style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily(Font(R.font.usual_medium)),
                            color = colorResource(R.color.blue900)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationOption(
    icon: Painter,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) colorResource(R.color.blue25) else Color.Transparent
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            colorResource(R.color.barneyPurple), colorResource(R.color.colorAccent)
        )
    )
    val borderModifier = if (selected) {
        Modifier.border(
            width = 1.dp, brush = gradientBrush, shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier.border(
            width = 1.dp, color = colorResource(R.color.blue100), shape = RoundedCornerShape(12.dp)
        )
    }

    Surface(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(borderModifier),
        color = backgroundColor,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = label, style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = colorResource(if (selected) R.color.blue900 else R.color.blue600)
                    )
                )
            }
        }
    }
}

@Composable
private fun InfoBanner(
    duration: LinkDuration, createdAt: LocalDate
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy") }
    val infoText = when (duration) {
        LinkDuration.NEVER -> stringResource(R.string.link_never_expires)
        else -> {
            val date = duration.expirationDate(createdAt)!!
            stringResource(R.string.link_expires_on, date.format(dateFormatter))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.warning50))
            .border(1.dp, colorResource(R.color.warning100), RoundedCornerShape(12.dp))
            .padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_info_orange),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = infoText, style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.warning800)
            )
        )
    }
}

@Composable
private fun RevokeLink(onClick: () -> Unit) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_revoke_link),
                contentDescription = null,
                tint = Color.Unspecified,
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.revoke_link), style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.error500)
                )
            )
        }
    }
}

enum class LinkDuration(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    ONE_DAY(
        labelRes = R.string.one_day, iconRes = R.drawable.ic_one_day_blue
    ),
    ONE_MONTH(
        labelRes = R.string.one_month, iconRes = R.drawable.ic_one_month_blue
    ),
    ONE_YEAR(
        labelRes = R.string.one_year, iconRes = R.drawable.ic_one_year_blue
    ),
    NEVER(
        labelRes = R.string.never, iconRes = R.drawable.ic_never_blue
    );

    fun expirationDate(from: LocalDate): LocalDate? = when (this) {
        ONE_DAY -> from.plusDays(1)
        ONE_MONTH -> from.plusMonths(1)
        ONE_YEAR -> from.plusYears(1)
        NEVER -> null
    }

    companion object {
        fun fromBackend(
            createdAt: String?,
            expirationTimestamp: String?
        ): LinkDuration {
            if (createdAt == null || expirationTimestamp == null) return NEVER

            val created = Instant.parse(createdAt)
            val expires = Instant.parse(expirationTimestamp)

            val duration = Duration.between(created, expires)

            return when {
                duration.toDays() <= 1 -> ONE_DAY
                duration.toDays() in 28..31 -> ONE_MONTH
                duration.toDays() in 360..370 -> ONE_YEAR
                else -> NEVER
            }
        }
    }
}