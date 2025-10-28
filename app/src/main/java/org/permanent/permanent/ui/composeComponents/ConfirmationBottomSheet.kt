@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun ConfirmationBottomSheet(
    message: String,
    boldText: String,
    confirmationButtonText: String = stringResource(id = R.string.delete),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val annotatedMessage = buildAnnotatedString {
        val startIndex = message.indexOf(boldText)
        val endIndex = startIndex + boldText.length

        append(message)

        if (startIndex >= 0) {
            // Apply bold style to the specific part
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = startIndex,
                end = endIndex
            )
        }
    }

    ModalBottomSheet(
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 48.dp)
        ) {
            Text(
                text = annotatedMessage,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Delete Button
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.RED,
                text = confirmationButtonText,
                icon = null,
                onButtonClick = onConfirm
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Button
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.LIGHT_BLUE,
                text = stringResource(id = R.string.cancel),
                icon = null,
                onButtonClick = onDismiss
            )
        }
    }
}