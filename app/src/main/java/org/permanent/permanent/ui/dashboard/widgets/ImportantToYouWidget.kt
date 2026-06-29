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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.dashboard.UsualFontFamily
import org.permanent.permanent.ui.dashboard.WidgetActionState

/** A selectable priority chip: label + the `why:*` tag it persists. */
private data class PriorityOption(val labelRes: Int, val tag: String)

// Chip labels from the design, mapped to the canonical onboarding `why:*` tags (the same tags
// ArchiveOnboardingViewModel.sendGoalsAndPriorities sends from the onboarding priorities step).
private val priorityOptions = listOf(
    PriorityOption(R.string.dashboard_priority_digital_preservation, "why:digipres"),
    PriorityOption(R.string.dashboard_priority_collaboration, "why:collaborate"),
    PriorityOption(R.string.dashboard_priority_family_history, "why:genealogy"),
    PriorityOption(R.string.dashboard_priority_secure_storage, "why:safe"),
    PriorityOption(R.string.dashboard_priority_nonprofit, "why:nonprofit"),
    PriorityOption(R.string.dashboard_priority_business, "why:professional"),
)

/**
 * "What's important to you?" priority chips (Figma frame 25366-22155).
 *
 * Selection is local; tapping **Save** persists the selected priorities as `why:*` account tags
 * (StelaAccountRepository.addRemoveTags) and dismisses the widget. **Remind me later** dismisses
 * without saving. Type spec from Figma: Usual throughout.
 */
@Composable
fun ImportantToYouWidget(
    state: WidgetActionState,
    onSave: (List<String>) -> Unit,
    onRemindLater: () -> Unit
) {
    val blue900 = colorResource(R.color.blue900)
    val blue600 = colorResource(R.color.blue600)
    val blue50 = colorResource(R.color.blue50)
    val blue25 = colorResource(R.color.blue25)
    val accent = colorResource(R.color.colorAccent)

    val enabled = state == WidgetActionState.Idle
    // Selection keyed by tag so Save can emit the tags directly.
    val selected = remember { mutableStateMapOf<String, Boolean>() }

    Box {
        DashboardCard {
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
                        painter = painterResource(id = R.drawable.ic_star_solid_accent),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.dashboard_important_title),
                        color = blue900,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = stringResource(R.string.dashboard_important_subtitle),
                        color = blue600,
                        fontFamily = UsualFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            HorizontalDivider(color = blue25)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                priorityOptions.forEach { option ->
                    val isSelected = selected[option.tag] == true
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isSelected) accent else blue25)
                            .border(
                                1.dp,
                                if (isSelected) accent else blue50,
                                RoundedCornerShape(99.dp)
                            )
                            .clickable(enabled = enabled) { selected[option.tag] = !isSelected }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(option.labelRes),
                            color = if (isSelected) Color.White else blue900,
                            fontFamily = UsualFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = blue25)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_remind_later),
                    // A bit transparent, mirroring the chart widget's 64%-opacity footer link.
                    color = blue600.copy(alpha = 0.64f),
                    fontFamily = UsualFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.clickable(enabled = enabled) { onRemindLater() }
                )
                Text(
                    text = stringResource(R.string.dashboard_save),
                    color = accent,
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
                overlayColor = OverlayColor.LIGHT,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
            )
        }
    }
}
