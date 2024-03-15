package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun TextAndIconButton(
    style: ButtonStyle,
    text: String,
    icon: Painter = painterResource(id = R.drawable.ic_arrow_next_primary),
    showButtonEnabled: Boolean,
    onButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val primaryColor200 = Color(ContextCompat.getColor(context, R.color.colorPrimary200))
    val subTitleTextSize = 16.sp
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))

    Button(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (style == ButtonStyle.LIGHT) whiteColor else if (showButtonEnabled) primaryColor else primaryColor200),
        shape = RoundedCornerShape(12.dp),
        onClick = { if (showButtonEnabled) onButtonClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (style == ButtonStyle.LIGHT) primaryColor else whiteColor,
                fontSize = subTitleTextSize,
                fontFamily = semiboldFont,
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Image(
                painter = icon,
                colorFilter = ColorFilter.tint(if (style == ButtonStyle.LIGHT) primaryColor else whiteColor),
                contentDescription = "Next",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

enum class ButtonStyle {
    LIGHT,
    DARK
}