package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun SmallTextAndIconButton(
    buttonColor: ButtonColor,
    text: String,
    icon: Painter = painterResource(id = R.drawable.ic_arrow_next_rounded_primary),
    iconAlignment: ButtonIconAlignment = ButtonIconAlignment.END,
    onButtonClick: () -> Unit
) {
    CustomSmallTextAndIconButton(
        buttonColor = buttonColor,
        text = text,
        annotatedText = null,
        icon = icon,
        iconAlignment = iconAlignment,
        onButtonClick = onButtonClick
    )
}

@Composable
fun SmallTextAndIconButton(
    buttonColor: ButtonColor,
    annotatedText: AnnotatedString,
    icon: Painter = painterResource(id = R.drawable.ic_arrow_next_rounded_primary),
    iconAlignment: ButtonIconAlignment = ButtonIconAlignment.END,
    onButtonClick: () -> Unit
) {
    CustomSmallTextAndIconButton(
        buttonColor = buttonColor,
        text = null,
        annotatedText = annotatedText,
        icon = icon,
        iconAlignment = iconAlignment,
        onButtonClick = onButtonClick
    )
}

@Composable
private fun CustomSmallTextAndIconButton(
    buttonColor: ButtonColor,
    text: String?,
    annotatedText: AnnotatedString?,
    icon: Painter = painterResource(id = R.drawable.ic_arrow_next_rounded_primary),
    iconAlignment: ButtonIconAlignment = ButtonIconAlignment.END,
    onButtonClick: () -> Unit
) {
    val context = LocalContext.current
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val whiteTransparentColor =
        Color(ContextCompat.getColor(context, R.color.whiteSuperTransparent))
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))

    Button(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(
            1.dp,
            if (buttonColor == ButtonColor.TRANSPARENT) whiteTransparentColor else Color.Transparent,
            RoundedCornerShape(12.dp)
        ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (buttonColor == ButtonColor.LIGHT) whiteColor else if (buttonColor == ButtonColor.TRANSPARENT) Color.Transparent else primaryColor),
        onClick = { onButtonClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconAlignment == ButtonIconAlignment.START) {
                Image(
                    painter = icon,
                    colorFilter = ColorFilter.tint(if (buttonColor == ButtonColor.LIGHT) primaryColor else whiteColor),
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))
            }

            if (text != null) {
                Text(
                    text = text,
                    color = if (buttonColor == ButtonColor.LIGHT) primaryColor else whiteColor,
                    fontSize = 16.sp,
                    fontFamily = semiboldFont,
                )
            } else if (annotatedText != null) {
                Text(
                    text = annotatedText,
                    color = if (buttonColor == ButtonColor.LIGHT) primaryColor else whiteColor,
                    fontSize = 16.sp,
                    fontFamily = semiboldFont,
                )
            }

            if (iconAlignment == ButtonIconAlignment.END) {
                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = icon,
                    colorFilter = ColorFilter.tint(if (buttonColor == ButtonColor.LIGHT) primaryColor else whiteColor),
                    contentDescription = "Next",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

enum class ButtonIconAlignment {
    START, END
}