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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole

@Composable
fun AccessRoleLabel(
    accessRole: AccessRole,
    fontSize: TextUnit = 10.sp,
    lineHeight: TextUnit = 24.sp,
    cornerSize: Dp = 6.dp,
    color: AccessRoleLabelColor = AccessRoleLabelColor.TRANSPARENT
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(cornerSize))
            .background(if (color == AccessRoleLabelColor.TRANSPARENT) Color.White.copy(alpha = 0.14f) else if (color == AccessRoleLabelColor.LIGHT_BLUE) colorResource(R.color.blue25) else Color.White)
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = accessRole.name,
            color = if (color == AccessRoleLabelColor.TRANSPARENT) Color.White else colorResource(R.color.colorPrimary),
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            fontSize = fontSize,
            lineHeight = lineHeight,
            letterSpacing = 1.28.sp
        )
    }
}

enum class AccessRoleLabelColor {
    LIGHT,
    TRANSPARENT,
    LIGHT_BLUE
}

@Preview
@Composable
fun AccessRoleLabelPreview() {
    AccessRoleLabel(AccessRole.OWNER)
}