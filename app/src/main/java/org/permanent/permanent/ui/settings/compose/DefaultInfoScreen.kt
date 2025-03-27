package org.permanent.permanent.ui.settings.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun DefaultInfoScreen(
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.superLightBlue)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_shield_blue),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.login_and_security),
            fontSize = 24.sp,
            lineHeight = 32.sp,
            color = colorResource(R.color.blue400),
            fontFamily = FontFamily(Font(R.font.usual_medium)),
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(id = R.string.login_and_security_description),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = colorResource(R.color.blue400),
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            textAlign = TextAlign.Center
        )
    }
}
