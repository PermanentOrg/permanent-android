package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole

@Composable
fun AccessRoleLabel(
    accessRole: AccessRole?
) {
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Column(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp)
    ) {
        accessRole?.name?.let {
            Text(
                text = it,
                color = Color.White,
                fontFamily = regularFont,
                fontSize = 10.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Preview
@Composable
fun AccessRoleLabelPreview() {
    AccessRoleLabel(AccessRole.OWNER)
}