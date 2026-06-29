@file:OptIn(ExperimentalLayoutApi::class)

package org.permanent.permanent.ui.dashboard.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.dashboard.PurpleMagentaGradient
import org.permanent.permanent.ui.dashboard.UsualFontFamily
import org.permanent.permanent.ui.dashboard.WidgetActionState
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** A selectable goal chip: label + the `goal:*` tag it persists. */
private data class GoalOption(val labelRes: Int, val tag: String)

// Figma card fill: linear-gradient(49.66deg, #800080 4.6%, #B843A6 95.5%) — purple lower-left to
// magenta upper-right. A size-aware ShaderBrush reproduces the CSS angle + stops at any card size.
private val ChartCardStops = floatArrayOf(0.046f, 0.955f)

private fun chartCardBrush(): ShaderBrush = object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val rad = Math.toRadians(49.656)
        val dx = sin(rad).toFloat()          // CSS angle is clockwise from "to top"
        val dy = -cos(rad).toFloat()
        val half = (abs(size.width * dx) + abs(size.height * dy)) / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f
        return LinearGradientShader(
            from = Offset(cx - dx * half, cy - dy * half),   // 0% → lower-left (#800080)
            to = Offset(cx + dx * half, cy + dy * half),     // 100% → upper-right (#B843A6)
            colors = PurpleMagentaGradient,
            colorStops = ChartCardStops.toList()
        )
    }
}

// Chip labels from the design, mapped to the canonical onboarding `goal:*` tags (the same tags
// ArchiveOnboardingViewModel.sendGoalsAndPriorities sends from the onboarding goals step).
private val goalOptions = listOf(
    GoalOption(R.string.dashboard_goal_publish, "goal:publish"),
    GoalOption(R.string.dashboard_goal_legacy, "goal:legacy"),
    GoalOption(R.string.dashboard_goal_share, "goal:share"),
    GoalOption(R.string.dashboard_goal_capture, "goal:capture"),
    GoalOption(R.string.dashboard_goal_digitize, "goal:digitize"),
    GoalOption(R.string.dashboard_goal_collaborate, "goal:collaborate"),
    GoalOption(R.string.dashboard_goal_organize, "goal:organize"),
)

// Figma overlay opacities of white on the purple→magenta card.
private val White64 = Color(0xA3FFFFFF)
private val White16 = Color(0x29FFFFFF)
private val White08 = Color(0x14FFFFFF)

/**
 * "Chart your path to success" goal chips (Figma node 25369:22270) — the goals counterpart to
 * [ImportantToYouWidget], on a purple→magenta gradient card.
 *
 * Selection is local; tapping **Save goals** persists the selected goals as `goal:*` account tags
 * (StelaAccountRepository.addRemoveTags) and dismisses the widget. **Remind me later** dismisses
 * without saving.
 */
@Composable
fun ChartYourPathWidget(
    state: WidgetActionState,
    onSave: (List<String>) -> Unit,
    onRemindLater: () -> Unit
) {
    val blue25 = colorResource(R.color.blue25)
    val enabled = state == WidgetActionState.Idle
    val selected = remember { mutableStateMapOf<String, Boolean>() }

    val cardShape = RoundedCornerShape(24.dp)
    val cardBrush = remember { chartCardBrush() }
    // Outer box (no padding) so the processing overlay can cover the full card, padding included.
    Box {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x14000000)
            )
            .clip(cardShape)
            .background(cardBrush)
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(blue25),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_trophy_star),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_chart_title),
                        color = Color.White,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = stringResource(R.string.dashboard_chart_subtitle),
                        color = White64,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            HorizontalDivider(color = White16)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                goalOptions.forEach { option ->
                    val isSelected = selected[option.tag] == true
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isSelected) Color.White else White08)
                            .border(1.dp, White16, RoundedCornerShape(99.dp))
                            .clickable(enabled = enabled) { selected[option.tag] = !isSelected }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (isSelected) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_check_gradient),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = stringResource(option.labelRes),
                            style = if (isSelected) {
                                TextStyle(brush = Brush.linearGradient(PurpleMagentaGradient))
                            } else {
                                TextStyle(color = Color.White)
                            },
                            fontFamily = UsualFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = White16)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_remind_later),
                    color = White64,
                    fontFamily = UsualFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.clickable(enabled = enabled) { onRemindLater() }
                )
                Text(
                    text = stringResource(R.string.dashboard_save_goals),
                    color = Color.White,
                    fontFamily = UsualFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.clickable(enabled = enabled) {
                        onSave(selected.filterValues { it }.keys.toList())
                    }
                )
            }
        }
    }
        if (state == WidgetActionState.Saving) {
            CircularProgressIndicator(
                overlayColor = OverlayColor.DARK,
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
            )
        }
    }
}
