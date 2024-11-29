package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun SettingsMenuItem(
    iconResource: Painter,
    text: String,
    itemColor: Color = Color(ContextCompat.getColor(LocalContext.current, R.color.colorPrimary)),
    showWarning: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = iconResource,
            contentDescription = "Next",
            colorFilter = ColorFilter.tint(itemColor),
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            color = itemColor,
            fontFamily = FontFamily(Font(R.font.usual_medium))
        )

        if (showWarning) {
            Spacer(modifier = Modifier.width(10.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_warning_red),
                contentDescription = "Warning",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
fun SettingsMenuItemComposablePreview() {
    SettingsMenuItem(
        iconResource = painterResource(id = R.drawable.ic_plus_primary),
        text = "Add storage!",
        showWarning = true,
        onClick = { /*TODO*/ })
}
