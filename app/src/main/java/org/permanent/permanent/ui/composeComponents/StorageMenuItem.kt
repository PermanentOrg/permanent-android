package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun StorageMenuItem(
    iconResource: Painter,
    title: String,
    subtitle: String,
    showNewLabel: Boolean,
    onClick: () -> Unit
) {

    val context = LocalContext.current

    val blue900Color = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val middleGreyColor = Color(ContextCompat.getColor(context, R.color.middleGrey))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val smallTextSize = 15.sp
    val superSmallTextSize = 13.sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        Image(
            painter = iconResource,
            contentDescription = "Next",
            modifier = Modifier.size(26.dp)
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
                    fontSize = smallTextSize,
                    color = blue900Color,
                    fontFamily = boldFont
                )

                if (showNewLabel) NewFeatureLabel()
            }
            Text(
                text = subtitle,
                fontSize = superSmallTextSize,
                color = middleGreyColor,
                fontFamily = regularFont
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_select_grey),
            contentDescription = "Next",
            colorFilter = ColorFilter.tint(middleGreyColor),
            modifier = Modifier.size(30.dp)
        )
    }
}

@Preview
@Composable
fun SimpleComposablePreview() {
    StorageMenuItem(
        iconResource = painterResource(id = R.drawable.ic_plus_primary),
        title = "Add storage!",
        subtitle = "Increase your space easily by adding more storage.",
        false,
        onClick = { /*TODO*/ })
}
