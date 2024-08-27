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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun SmallTextAndIconButton(
    buttonColor: ButtonColor,
    text: String,
    fontSize: TextUnit = 16.sp,
    icon: Painter? = painterResource(id = R.drawable.ic_arrow_next_rounded_primary),
    iconAlignment: ButtonIconAlignment = ButtonIconAlignment.END,
    showButtonEnabled: Boolean = true,
    onButtonClick: () -> Unit
) {
    CustomSmallTextAndIconButton(
        buttonColor = buttonColor,
        text = text,
        fontSize = fontSize,
        annotatedText = null,
        icon = icon,
        iconAlignment = iconAlignment,
        showButtonEnabled = showButtonEnabled,
        onButtonClick = onButtonClick
    )
}

@Composable
fun SmallTextAndIconButton(
    buttonColor: ButtonColor,
    annotatedText: AnnotatedString,
    fontSize: TextUnit = 16.sp,
    icon: Painter? = painterResource(id = R.drawable.ic_arrow_next_rounded_primary),
    iconAlignment: ButtonIconAlignment = ButtonIconAlignment.END,
    showButtonEnabled: Boolean = true,
    onButtonClick: () -> Unit
) {
    CustomSmallTextAndIconButton(
        buttonColor = buttonColor,
        text = null,
        fontSize = fontSize,
        annotatedText = annotatedText,
        icon = icon,
        iconAlignment = iconAlignment,
        showButtonEnabled = showButtonEnabled,
        onButtonClick = onButtonClick
    )
}

@Composable
private fun CustomSmallTextAndIconButton(
    buttonColor: ButtonColor,
    text: String?,
    annotatedText: AnnotatedString?,
    fontSize: TextUnit = 16.sp,
    icon: Painter? = painterResource(id = R.drawable.ic_arrow_next_rounded_primary),
    iconAlignment: ButtonIconAlignment = ButtonIconAlignment.END,
    showButtonEnabled: Boolean,
    onButtonClick: () -> Unit
) {
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val primaryColor = colorResource(id = R.color.colorPrimary)
    val whiteTransparentColor = colorResource(id = R.color.whiteSuperTransparent)

    Button(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(
            1.dp,
            if (buttonColor == ButtonColor.TRANSPARENT) whiteTransparentColor else Color.Transparent,
            RoundedCornerShape(12.dp)
        ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (!showButtonEnabled) whiteTransparentColor else if (buttonColor == ButtonColor.LIGHT) Color.White else if (buttonColor == ButtonColor.TRANSPARENT) Color.Transparent else primaryColor),
        onClick = { if (showButtonEnabled) onButtonClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconAlignment == ButtonIconAlignment.START && icon != null) {
                Image(
                    painter = icon,
                    colorFilter = ColorFilter.tint(if (buttonColor == ButtonColor.LIGHT) primaryColor else Color.White),
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))
            }

            if (text != null) {
                Text(
                    text = text,
                    color = if (buttonColor == ButtonColor.LIGHT) primaryColor else Color.White,
                    fontSize = fontSize,
                    fontFamily = semiboldFont,
                )
            } else if (annotatedText != null) {
                Text(
                    text = annotatedText,
                    color = if (buttonColor == ButtonColor.LIGHT) primaryColor else Color.White,
                    fontSize = fontSize,
                    fontFamily = semiboldFont,
                )
            }

            if (iconAlignment == ButtonIconAlignment.END && icon != null) {
                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = icon,
                    colorFilter = ColorFilter.tint(if (buttonColor == ButtonColor.LIGHT) primaryColor else Color.White),
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

@Preview
@Composable
fun SmallTextAndIconButtonPreview() {
    SmallTextAndIconButton(
        buttonColor = ButtonColor.TRANSPARENT,
        text = stringResource(id = R.string.accept),
    ) { }
}