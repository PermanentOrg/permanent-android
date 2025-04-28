package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.ui.bytesToCustomHumanReadableString

@Composable
fun StorageCard(
    spaceUsedBytes: Long,
    spaceTotalBytes: Long,
    spaceUsedPercentage: Int,
    style: StorageCardStyle = StorageCardStyle.COLORFUL
) {
    val context = LocalContext.current

    val purpleColor = Color(ContextCompat.getColor(context, R.color.barneyPurple))
    val accentColor = Color(ContextCompat.getColor(context, R.color.colorAccent))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val whiteTransparentColor =
        Color(ContextCompat.getColor(context, R.color.whiteSuperExtraTransparent))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.blue25))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val superSmallTextSize = 13.sp

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .background(
                brush = if (style == StorageCardStyle.LIGHT) Brush.horizontalGradient(
                    colors = listOf(
                        whiteColor, whiteColor
                    )
                ) else Brush.horizontalGradient(
                    colors = listOf(
                        purpleColor, accentColor
                    )
                ), RoundedCornerShape(16.dp)
            ), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                text = bytesToCustomHumanReadableString(spaceUsedBytes, true) + " used",
                fontSize = superSmallTextSize,
                color = if (style == StorageCardStyle.LIGHT) blackColor else whiteColor,
                fontFamily = semiboldFont
            )
            Text(
                modifier = Modifier.padding(top = 16.dp, end = 16.dp),
                text = bytesToCustomHumanReadableString(spaceTotalBytes, false) + "",
                fontSize = superSmallTextSize,
                color = if (style == StorageCardStyle.LIGHT) blackColor else whiteColor,
                fontFamily = semiboldFont
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            CustomLinearProgressIndicator(
                Modifier
                    .clip(shape = RoundedCornerShape(3.dp))
                    .height(8.dp),
                width = maxWidth,
                if (style == StorageCardStyle.LIGHT) lightBlueColor else whiteTransparentColor,
                if (style == StorageCardStyle.LIGHT) Brush.horizontalGradient(
                    listOf(
                        purpleColor, accentColor
                    )
                ) else Brush.horizontalGradient(listOf(whiteColor, whiteColor)),
                spaceUsedPercentage
            )
        }
    }
}

enum class StorageCardStyle {
    LIGHT, COLORFUL
}