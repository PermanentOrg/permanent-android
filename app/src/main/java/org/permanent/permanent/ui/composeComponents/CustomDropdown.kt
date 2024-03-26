package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun CustomDropdown() {
    val context = LocalContext.current
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val purpleColor = Color(ContextCompat.getColor(context, R.color.barneyPurple))
    val accentColor = Color(ContextCompat.getColor(context, R.color.colorAccent))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Button(modifier = Modifier
        .fillMaxWidth()
        .height(112.dp)
        .background(
            Brush.horizontalGradient(listOf(purpleColor, accentColor)),
            RoundedCornerShape(12.dp)
        ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape =  RoundedCornerShape(12.dp),
        onClick = { }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_heart_white),
                colorFilter = ColorFilter.tint(whiteColor),
                contentDescription = "",
                modifier = Modifier.size(16.dp)
            )

            Column(
                modifier = Modifier.weight(1.0f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Individual",
                    color = whiteColor,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = semiboldFont,
                )

                Text(
                    text = "Create an archive that captures a person’s life.",
                    color = whiteColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = regularFont,
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                colorFilter = ColorFilter.tint(whiteColor),
                contentDescription = "Drop down",
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
