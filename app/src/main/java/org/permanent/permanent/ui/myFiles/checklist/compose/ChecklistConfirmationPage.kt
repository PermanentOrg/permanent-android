package org.permanent.permanent.ui.myFiles.checklist.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton

@Composable
fun ChecklistConfirmationPage(
    onConfirmClick: () -> Unit, onDismissClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally),
            painter = painterResource(R.drawable.ic_eye_off),
            contentDescription = null,
            tint = colorResource(R.color.blue400)
        )

        Text(
            text = stringResource(R.string.dont_show_again_description),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue700)
            )
        )

        // Spacer to push buttons to bottom of remaining space
        Spacer(modifier = Modifier.weight(1f))

        CenteredTextAndIconButton(
            buttonColor = ButtonColor.DARK,
            text = stringResource(id = R.string.yes_button),
            icon = null,
            onButtonClick = onConfirmClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        CenteredTextAndIconButton(
            buttonColor = ButtonColor.LIGHT_BLUE,
            text = stringResource(id = R.string.button_cancel),
            icon = null,
            onButtonClick = onDismissClick
        )
    }
}