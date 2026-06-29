package org.permanent.permanent.ui.dashboard.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.dashboard.UsualFontFamily

/**
 * "Hello, {name} 👋 / Your memories are safe here!" — the Dashboard greeting header.
 * Type spec from Figma: Usual Medium 16 (#131B4A) over Usual Regular 14 (#5A5F80).
 */
@Composable
fun GreetingWidget(firstName: String) {
    val blue900 = colorResource(R.color.blue900)
    val blue600 = colorResource(R.color.blue600)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_dashboard_avatar),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

        Column {
            Text(
                text = if (firstName.isBlank()) "Hello 👋" else "Hello, $firstName 👋",
                color = blue900,
                fontFamily = UsualFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = (-0.16).sp
            )
            Text(
                text = "Your memories are safe here!",
                color = blue600,
                fontFamily = UsualFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 24.sp
            )
        }
    }
}
