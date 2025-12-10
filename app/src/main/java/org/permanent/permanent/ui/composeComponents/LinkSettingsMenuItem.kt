package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun LinkSettingsMenuItem(
    isTablet: Boolean = false,
    iconResource: Painter,
    title: String,
    subtitle: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) colorResource(id = R.color.blue25).copy(alpha = 0.5f) else Color.Transparent)
            .padding(24.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)

    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isSelected) Color.White else colorResource(R.color.success50), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = iconResource,
                contentDescription = "",
                tint = Color.Unspecified
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1.0f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = title,
                fontSize = if (isTablet) 18.sp else 14.sp,
                lineHeight = 24.sp,
                color = colorResource(R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_medium))
            )

            Text(
                text = subtitle,
                fontSize = if (isTablet) 18.sp else 12.sp,
                lineHeight = if (isTablet) 32.sp else 16.sp,
                color = colorResource(R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_regular))
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle_green),
                    contentDescription = "Checklist Icon",
                    tint = Color.Unspecified
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_circle_blue),
                contentDescription = "",
                tint = Color.Unspecified
            )
        }
    }
}

@Preview
@Composable
fun LinkSettingsComposablePreview() {
    LinkSettingsMenuItem(
        iconResource = painterResource(id = R.drawable.ic_plus_primary),
        title = "Add storage!",
        subtitle = "Increase your space easily by adding more storage.",
        isSelected = true,
        onClick = { })
}
