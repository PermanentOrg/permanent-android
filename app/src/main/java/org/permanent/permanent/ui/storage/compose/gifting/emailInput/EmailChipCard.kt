package org.permanent.permanent.ui.storage.compose.gifting.emailInput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun EmailChipCard(text: String,
                  onDelete: (text: String) -> Unit) {

    val context = LocalContext.current
    val superLightBlue = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val colorPrimary = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Box(
            modifier = Modifier
                .background(
                    color = superLightBlue,
                    shape = RoundedCornerShape(8.dp)
                )
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                Text(
                    text = text,
                    color = colorPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    fontSize = 14.sp,
                    fontFamily = regularFont
                )
                IconButton(
                    onClick = {
                        onDelete(text)
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    // Replace the Icons.Filled.Favorite with your desired ImageVector
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_middle_grey),
                        contentDescription = "Delete Email",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }

}

@Preview
@Composable
fun SimpleChipPreview() {
    EmailChipCard(text = "hello", onDelete = {

    })
}