package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.USAOnlyLabel

@Composable
fun MethodMenuItem(
    iconResource: Painter,
    text: String,
    showUSAOnlyLabel: Boolean = false,
    selected: Boolean = false,
    onSelectedChange: () -> Unit,
) {
    val blue900Color = colorResource(R.color.blue900)

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onSelectedChange() } // Select when clicked
        .background(color = colorResource(if (selected) R.color.blue25 else R.color.white))
        .padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        // Icon on the left
        Image(
            painter = iconResource,
            contentDescription = "",
            colorFilter = ColorFilter.tint(blue900Color),
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        // Row for Text and Label to keep them together
        Row(
            modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = blue900Color,
                fontFamily = FontFamily(Font(if (selected) R.font.usual_medium else R.font.usual_regular))
            )

            if (showUSAOnlyLabel) {
                Spacer(modifier = Modifier.width(8.dp))
                USAOnlyLabel(isSelected = selected)
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Custom RadioButton with different icons at the end of the row
        Icon(painter = painterResource(
            id = if (selected) R.drawable.ic_radio_selected else R.drawable.ic_radio_unselected
        ),
            contentDescription = if (selected) "Selected" else "Unselected",
            modifier = Modifier.clickable { onSelectedChange() })
    }
}


@Preview
@Composable
fun ComposablePreview() {
    MethodMenuItem(iconResource = painterResource(id = R.drawable.ic_plus_primary),
        text = "Add storage!",
        showUSAOnlyLabel = true,
        selected = true,
        onSelectedChange = {})
}
