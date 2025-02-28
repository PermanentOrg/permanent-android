package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton

@Composable
fun PasswordConfirmationPage(
    onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with Centered Title and Close Button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Centered Title
            Text(
                text = stringResource(R.string.confirm_your_password),
                color = colorResource(R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

            // Custom Close Button
            IconButton(
                onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_middle_grey),
                    contentDescription = "Close",
                    tint = colorResource(R.color.blue900),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider below the header
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(), color = colorResource(R.color.blue50)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Instruction Text
            Text(
                text = stringResource(R.string.confirm_your_password_description),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue)
            )

            Spacer(modifier = Modifier.height(32.dp))

            var password by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp, colorResource(R.color.blue100), RoundedCornerShape(12.dp)
                    ), verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = password,
                    onValueChange = { value -> password = value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .weight(1.0f),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.password).uppercase(),
                            color = colorResource(R.color.blue900),
                            fontSize = 10.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily(Font(R.font.usual_regular))
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        fontWeight = FontWeight(600),
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.blue900),
                        unfocusedTextColor = colorResource(R.color.blue900),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = colorResource(id = R.color.blue400)
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Button
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.DARK,
                text = stringResource(id = R.string.confirm_password),
                icon = null
            ) {
                keyboardController?.hide()
                onConfirm(password)
            }
        }
    }
}
