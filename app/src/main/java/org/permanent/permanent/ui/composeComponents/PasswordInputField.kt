package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun PasswordInputField(
    value: String, onValueChange: (String) -> Unit, showToggle: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorResource(id = R.color.blue200), RoundedCornerShape(10.dp)),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(
            fontSize = 14.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily(Font(R.font.pt_mono)),
            fontWeight = FontWeight(400),
            color = colorResource(id = R.color.blue900),
            letterSpacing = 1.12.sp,
        ),

        placeholder = {
            Text(
                text = stringResource(id = R.string.password).uppercase(),
                fontSize = 10.sp,
                color = colorResource(id = R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                letterSpacing = 1.6.sp,
            )
        },
        visualTransformation = if (!showToggle || !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (showToggle) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_eye_off_blue
                            else R.drawable.ic_eye_blue
                        ), contentDescription = null, tint = Color.Unspecified
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = colorResource(id = R.color.blue900),
            unfocusedTextColor = colorResource(id = R.color.blue900)
        )
    )
}