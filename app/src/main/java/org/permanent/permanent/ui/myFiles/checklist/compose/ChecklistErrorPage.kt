package org.permanent.permanent.ui.myFiles.checklist.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.permanent.permanent.viewmodels.ChecklistViewModel

@Composable
fun ChecklistErrorPage(
    viewModel: ChecklistViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.padding(bottom = 16.dp),
            painter = painterResource(R.drawable.ic_error_circle_empty),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Text(
            text = stringResource(R.string.something_went_wrong),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.blue900)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.something_went_wrong_description),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue700)
            )
        )

        // Spacer to push button to bottom of remaining space
        Spacer(modifier = Modifier.weight(1f))

        CenteredTextAndIconButton(buttonColor = ButtonColor.DARK,
            text = stringResource(id = R.string.refresh),
            icon = painterResource(R.drawable.ic_refresh_white),
            iconSize = 24.dp,
            onButtonClick = { viewModel.getChecklist() })
    }
}