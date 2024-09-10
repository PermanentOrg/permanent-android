package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun CustomSnackbar(
    isTablet: Boolean = false,
    message: String, buttonText: String, onButtonClick: () -> Unit, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(if (isTablet) 64.dp else 32.dp)
            .background(
                color = colorResource(id = R.color.errorLight),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(24.dp), contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_error_cercle),
                contentDescription = "error",
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = message,
                color = colorResource(id = R.color.error500),
                modifier = Modifier.weight(1f),
                fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                fontSize = 14.sp,
                lineHeight = 24.sp
            )

            TextButton(onClick = onButtonClick) {
                Text(
                    text = buttonText,
                    color = colorResource(id = R.color.blue900),
                    fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf)),
                    fontSize = 14.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun CustomSnackbarPreview() {
    CustomSnackbar(message = "The entered data is invalid",
        buttonText = "OK",
        onButtonClick = { /*TODO*/ })
}
