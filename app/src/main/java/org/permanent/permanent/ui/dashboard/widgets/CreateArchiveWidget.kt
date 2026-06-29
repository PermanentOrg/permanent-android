package org.permanent.permanent.ui.dashboard.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.dashboard.CreateArchiveState
import org.permanent.permanent.ui.dashboard.DashboardDisplayFont
import org.permanent.permanent.ui.dashboard.NavyTitleGradient
import org.permanent.permanent.ui.dashboard.PurpleOrangeTitleGradient
import org.permanent.permanent.ui.dashboard.UsualFontFamily

/**
 * The primary, fully-wired widget. Renders one of two states:
 *  - Idle/Creating: the "Let's begin your archive" call-to-action that opens the create sheet.
 *  - Success: the "Your archive is ready!" confirmation with a "Go to Archive" button.
 *
 * Hero titles use the serif display face + gradient text fill from the design; all other copy
 * uses the Usual family at the design's exact sizes/weights/colors.
 */
@Composable
fun CreateArchiveWidget(
    state: CreateArchiveState,
    onCreateClick: () -> Unit,
    onGoToArchiveClick: () -> Unit
) {
    when (state) {
        is CreateArchiveState.Success -> ArchiveReadyContent(
            archive = state.archive,
            onGoToArchiveClick = onGoToArchiveClick
        )

        else -> BeginArchiveContent(
            isCreating = state == CreateArchiveState.Creating,
            onCreateClick = onCreateClick
        )
    }
}

@Composable
private fun BeginArchiveContent(isCreating: Boolean, onCreateClick: () -> Unit) {
    val blue600 = colorResource(R.color.blue600)

    // Card padding from design: top 48, bottom 24, sides 24; groups separated by 48.
    DashboardCard(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // "Let's begin your archive" — serif display, purple→orange gradient, italic word.
                GradientDisplayTitle(
                    text = buildAnnotatedString {
                        append("Let's begin your ")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("archive") }
                    },
                    gradient = PurpleOrangeTitleGradient
                )

                Text(
                    text = stringResource(R.string.dashboard_create_archive_subtitle),
                    color = blue600,
                    fontFamily = UsualFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.DARK,
                    text = stringResource(R.string.dashboard_create_first_archive_button),
                    fontSize = 14.sp,
                    icon = null,
                    enabled = !isCreating,
                    onButtonClick = onCreateClick
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_lock_green),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.dashboard_create_archive_privacy_note),
                    color = blue600,
                    fontFamily = UsualFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ArchiveReadyContent(archive: Archive, onGoToArchiveClick: () -> Unit) {
    val blue900 = colorResource(R.color.blue900)
    val blue400 = colorResource(R.color.blue400)
    // Backend fullName is already "The {name} Archive"; strip the affixes to the core name so we
    // don't render "The The … Archive Archive" (and so the initials come from the real name).
    val coreName = coreArchiveName(archive.fullName)

    DashboardCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // "🎉 Your archive is ready!" — emoji + serif display, navy gradient, italic word.
            // Title group carries 24 vertical padding per design.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉", fontSize = 40.sp, lineHeight = 40.sp)
                GradientDisplayTitle(
                    text = buildAnnotatedString {
                        append("Your ")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append("archive") }
                        append(" is ready!")
                    },
                    gradient = NavyTitleGradient
                )
            }

            // Avatar + name group (gap 16).
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Archive avatar — 40dp gradient square + white tab + initials, per design.
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color(0xFF8D0085),
                                    androidx.compose.ui.graphics.Color(0xFFFF9400)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .size(width = 16.dp, height = 2.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colorResource(R.color.white))
                    )
                    Text(
                        // Sit slightly below center, under the white tab, per design.
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        text = initialsOf(coreName),
                        color = colorResource(R.color.white),
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // "The {name} Archive" — Usual Regular 14, name in Medium.
                    Text(
                        text = buildAnnotatedString {
                            append("The ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                append(coreName)
                            }
                            append(" Archive")
                        },
                        color = blue900,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.dashboard_archive_ready_meta),
                        color = blue400,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            CenteredTextAndIconButton(
                buttonColor = ButtonColor.DARK,
                text = stringResource(R.string.dashboard_go_to_archive_button),
                fontSize = 14.sp,
                iconAlignment = ButtonIconAlignment.END,
                onButtonClick = onGoToArchiveClick
            )
        }
    }
}

/** Hero title: serif display face, 40sp, gradient text fill (Figma "Gyst Variable" titles). */
@Composable
fun GradientDisplayTitle(
    text: androidx.compose.ui.text.AnnotatedString,
    gradient: List<androidx.compose.ui.graphics.Color>
) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        style = TextStyle(
            brush = Brush.linearGradient(gradient),
            fontFamily = DashboardDisplayFont,
            fontWeight = FontWeight.Medium,
            fontSize = 40.sp,
            lineHeight = 40.sp,
            letterSpacing = (-1.2).sp
        )
    )
}

/** Shared white rounded card used by all Dashboard widgets. Radius 24, padding 24 per design. */
@Composable
fun DashboardCard(
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Figma "Widget Drop": a soft, very faint downward shadow (~2–4% black). Compose
            // can't stack two custom drop shadows with spread, so approximate with one soft,
            // low-opacity elevation.
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = androidx.compose.ui.graphics.Color(0x0A000000),
                spotColor = androidx.compose.ui.graphics.Color(0x14000000)
            )
            .clip(cardShape)
            .background(colorResource(R.color.white))
            .padding(contentPadding)
    ) {
        content()
    }
}

/** Strips Permanent's "The …/… Archive" affixes to the core archive name (e.g. "Robert Friedman"). */
private fun coreArchiveName(fullName: String?): String {
    val full = fullName?.trim().orEmpty()
    val core = full.removePrefix("The ").removeSuffix(" Archive").trim()
    return core.ifBlank { full }
}

private fun initialsOf(fullName: String?): String {
    val parts = fullName?.trim()?.split(" ")?.filter { it.isNotBlank() }.orEmpty()
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
