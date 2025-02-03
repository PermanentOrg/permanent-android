package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun USAOnlyLabel(isSelected: Boolean) {
    val selectedColor = Color.White
    val unselectedColor = colorResource(R.color.blue25)

    Column(
        modifier = Modifier
            .background(color = if (isSelected) selectedColor else unselectedColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 1.dp)
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.usa_only).uppercase(),
            fontSize = 10.sp,
            lineHeight = 24.sp,
            letterSpacing = 1.6.sp,
            color = colorResource(R.color.blue900),
            fontFamily = FontFamily(Font(R.font.usual_medium))
        )
    }
}

@Preview
@Composable
fun USAOnlyLabelPreview() {
    USAOnlyLabel(true)
}