package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun MenuItem(
    isTablet: Boolean = false,
    iconResource: Painter,
    iconSize: Dp = 18.dp,
    title: String,
    subtitle: String,
    isSelected: Boolean = false,
    showNewLabel: Boolean = false,
    showOffLabel: Boolean = false,
    showOnLabel: Boolean = false,
    showArrow: Boolean = false,
    showSwitch: Boolean = false,
    switchChecked: Boolean = false,
    onSwitchCheckedChange: (Boolean) -> Unit = {},
    onClick: () -> Unit
) {

    val context = LocalContext.current

    val blue900Color = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val middleGreyColor = Color(ContextCompat.getColor(context, R.color.middleGrey))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(end = 24.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        // Selection Indicator (Colored Bar)
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(if (isSelected) blue900Color else Color.Transparent)
        )

        Spacer(Modifier.width(20.dp))

        Image(
            painter = iconResource,
            contentDescription = "",
            colorFilter = ColorFilter.tint(blue900Color),
            modifier = Modifier
                .padding(top = 26.dp)
                .size(iconSize)
        )
        Column(
            modifier = Modifier
                .padding(24.dp)
                .weight(1.0f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = if (isTablet) 18.sp else 14.sp,
                    lineHeight = 24.sp,
                    color = blue900Color,
                    fontFamily = FontFamily(Font(R.font.usual_bold))
                )

                if (showNewLabel) MenuLabel(
                    colorResource(id = R.color.colorAccent), stringResource(id = R.string.new_label)
                )
                if (showOffLabel) MenuLabel(
                    colorResource(id = R.color.error500), stringResource(id = R.string.off)
                )
                if (showOnLabel) MenuLabel(
                    colorResource(id = R.color.success500), stringResource(id = R.string.on)
                )
            }
            Text(
                text = subtitle,
                fontSize = if (isTablet) 18.sp else 12.sp,
                lineHeight = if (isTablet) 32.sp else 16.sp,
                color = middleGreyColor,
                fontFamily = FontFamily(Font(R.font.usual_regular))
            )
        }

        if (showArrow) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_select_light_blue),
                contentDescription = "Next",
                modifier = Modifier
                    .padding(top = 26.dp)
                    .size(14.dp)
            )
        }

        if (showSwitch) {
            Switch(
                checked = switchChecked,
                onCheckedChange = onSwitchCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorResource(id = R.color.white),
                    checkedTrackColor = colorResource(id = R.color.success500),
                ),
                modifier = Modifier
                    .padding(top = 14.dp)
                    .scale(0.7f)
            )
        }
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    MenuItem(iconResource = painterResource(id = R.drawable.ic_plus_primary),
        title = "Add storage!",
        subtitle = "Increase your space easily by adding more storage.",
        isSelected = true,
        showNewLabel = true,
        showSwitch = true,
        onClick = { })
}
