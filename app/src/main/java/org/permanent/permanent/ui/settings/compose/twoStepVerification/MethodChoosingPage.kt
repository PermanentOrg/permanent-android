package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton

@Composable
fun MethodChoosingPage(
    onDismiss: () -> Unit,
    onContinue: (VerificationMethod) -> Unit
) {
    var selectedMethod by remember { mutableStateOf<VerificationMethod?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Centered Title and Close Button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Centered Title
            Text(
                text = stringResource(R.string.choose_a_verification_method),
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
                .weight(1f) // Pushes remaining content to the top
        ) {
            // Email option
            MethodMenuItem(
                iconResource = painterResource(id = R.drawable.ic_email_blue),
                text = stringResource(id = R.string.email),
                selected = selectedMethod == VerificationMethod.EMAIL,
                onSelectedChange = { selectedMethod = VerificationMethod.EMAIL }
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(), color = colorResource(R.color.blue50)
            )

            // SMS option
            MethodMenuItem(
                iconResource = painterResource(id = R.drawable.ic_sms_blue),
                text = stringResource(id = R.string.text_message),
                selected = selectedMethod == VerificationMethod.SMS,
                showUSAOnlyLabel = true,
                onSelectedChange = { selectedMethod = VerificationMethod.SMS }
            )
        }

        // Continue Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 48.dp, start = 32.dp, end = 32.dp)
        ) {
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.DARK,
                text = stringResource(id = R.string.button_continue),
                icon = null,
                enabled = selectedMethod != null,
                disabledColor = colorResource(R.color.colorPrimary200)
            ) {
                selectedMethod?.let { onContinue(it) }
            }
        }
    }
}

enum class VerificationMethod {
    EMAIL, SMS
}
