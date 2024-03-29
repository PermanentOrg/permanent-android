package org.permanent.permanent.ui.bulkEditMetadata.compose

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun NewTagView(onClick: () -> Unit) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val primaryColorTransparent = primaryColor.copy(alpha = 0.1f)
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val unselectedBackgroundColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))

    Box(modifier = Modifier
        .padding(top = 8.dp)
        .clickable { onClick() }) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .border(1.dp, primaryColorTransparent, RoundedCornerShape(8.dp))
                .background(unselectedBackgroundColor)
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_plus_primary),
                    contentDescription = "Description",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.new_tag),
                    color = primaryColor,
                    fontFamily = regularFont,
                    fontSize = 14.sp
                )
            }
        }
    }
}