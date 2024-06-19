package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun LocationDetails(primaryText: String,
                            secondaryText: String,
                            didSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable {
                didSelect()
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_location_icon),
            contentDescription = "Location icon",
            contentScale = ContentScale.None,
            modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .background(color = Color(0xFFF4F6FD), shape = RoundedCornerShape(size = 2.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column() {
            Text(
                text = primaryText,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF606060),
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = secondaryText,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFB4B4B4),
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}