package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.formatPendingInvitationCount

@Composable
fun PendingInvitationBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    val text = formatPendingInvitationCount(count)
    Box(
        modifier = modifier
            .width(20.dp)
            .height(16.dp)
            .background(colorResource(R.color.error500), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 8.sp,
            letterSpacing = 1.28.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily(Font(R.font.usual_bold))
        )
    }
}

@Preview
@Composable
fun PendingInvitationBadgePreviewSingle() {
    PendingInvitationBadge(count = 2)
}

@Preview
@Composable
fun PendingInvitationBadgePreviewMany() {
    PendingInvitationBadge(count = 42)
}
