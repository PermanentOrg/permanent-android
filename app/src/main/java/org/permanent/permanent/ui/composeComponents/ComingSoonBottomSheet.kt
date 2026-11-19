@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun ComingSoonBottomSheet(message: String, onDismiss: () -> Unit) {
    ModalBottomSheet(
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = stringResource(R.string.coming_soon_to_mobile).uppercase(),
                modifier = Modifier
                    .background(colorResource(R.color.warning100), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp)
                    .semantics { heading() },
                fontSize = 10.sp,
                lineHeight = 24.sp,
                letterSpacing = 1.6.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.warning500),
            )

            Image(painter = painterResource(R.drawable.ic_edit_on_web), contentDescription = null)

            Text(
                text = message,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue),
                textAlign = TextAlign.Center,
            )

            CenteredTextAndIconButton(
                buttonColor = ButtonColor.LIGHT_BLUE,
                text = stringResource(R.string.got_it),
                icon = null,
                onButtonClick = onDismiss
            )
        }
    }
}
