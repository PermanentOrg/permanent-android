package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun CustomTextButton(
    style: ButtonColor = ButtonColor.LIGHT,
    text: String,
    icon: Painter? = null,
    onButtonClick: () -> Unit
) {
    Button(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        onClick = { onButtonClick() }) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    color = if (style == ButtonColor.LIGHT) Color.White else colorResource(id = R.color.blue900),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf))
                )

                if (icon != null) {
                    Spacer(modifier = Modifier.width(16.dp))

                    Image(
                        painter = icon, colorFilter = ColorFilter.tint(
                            if (style == ButtonColor.LIGHT) Color.White else colorResource(
                                id = R.color.blue900
                            )
                        ), contentDescription = "Back", modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TextButtonPreview() {
    CustomTextButton(
        text = stringResource(id = R.string.forgot_your_password_title),
    ) { }
}