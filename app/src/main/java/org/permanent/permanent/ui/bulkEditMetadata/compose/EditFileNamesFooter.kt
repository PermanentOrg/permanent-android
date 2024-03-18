package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun EditFileNamesFooter() {
    val context = LocalContext.current
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val lightGrey = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val middleGrey = Color(ContextCompat.getColor(context, R.color.middleGrey))
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))

    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text(
                text = "Preview",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = semiBoldFont,
                    color = lightGrey,
                )
            )
            Divider()
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_checkbox_half_full),
                contentDescription = "image description",
                contentScale = ContentScale.FillBounds
            )
            Column() {
                Text(
                    text = "Seattle — 3912_iPhone.JPG",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = regularFont,
                        color = middleGrey,
                    )
                )
                Text(
                    text = "930 KB",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = regularFont,
                        color = lightGrey,
                    )
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = lightBlueColor),
                onClick = { }
            ) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    fontSize = 14.sp,
                    color = primaryColor,
                    fontFamily = regularFont,
                )
            }

            Button(modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .height(48.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                onClick = {

                }
            ) {
                Text(
                    text = "Apply Changes",
                    fontSize = 14.sp,
                    fontFamily = regularFont,
                )
            }
        }
    }
}