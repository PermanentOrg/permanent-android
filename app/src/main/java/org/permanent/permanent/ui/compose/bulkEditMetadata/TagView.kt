package org.permanent.permanent.ui.compose.bulkEditMetadata

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun TagView(
    text: String, isSelected: State<Boolean?>?, onTagClick: () -> Unit, onTagRemoveClick: () -> Unit
) {
    val context = LocalContext.current

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val selectedBackgroundColor =
        Color(ContextCompat.getColor(context, R.color.colorAccent)).copy(alpha = 0.3f)
    val unselectedBackgroundColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))

    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .clickable {
                if (isSelected?.value == null || isSelected.value == false) {
                    onTagClick()
                }
            }) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .then(
                    if (isSelected?.value == null || isSelected.value == false) {
                        Modifier.border(1.dp, selectedBackgroundColor, RoundedCornerShape(8.dp))
                    } else {
                        Modifier.clip(RoundedCornerShape(8.dp))
                    }
                )
                .background(if (isSelected?.value == true) selectedBackgroundColor else unselectedBackgroundColor)
                .padding(
                    horizontal = 12.dp, vertical = 10.dp
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = text, color = primaryColor, fontFamily = regularFont, fontSize = 14.sp
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_close_white),
                    contentDescription = "Description",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onTagRemoveClick() },
                    colorFilter = ColorFilter.tint(lightGreyColor)
                )
            }
        }
    }
}