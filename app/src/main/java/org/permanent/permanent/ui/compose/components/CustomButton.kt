package org.permanent.permanent.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun CustomButton(text: String, showButtonEnabled: Boolean, onButtonClick: () -> Unit) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val primaryColor200 = Color(ContextCompat.getColor(context, R.color.colorPrimary200))
    val subTitleTextSize = 16.sp
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Button(modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (showButtonEnabled) primaryColor else primaryColor200),
        shape = RoundedCornerShape(8.dp),
        onClick = { if (showButtonEnabled) onButtonClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = subTitleTextSize,
                fontFamily = regularFont,
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_next_white),
                contentDescription = "Next",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}