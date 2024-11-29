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
fun MenuLabel(colorResource: Color, stringResource: String) {

    Column(
        modifier = Modifier
            .background(colorResource, RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 1.dp)
    ) {
        Text(
            modifier = Modifier,
            text = stringResource.uppercase(),
            fontSize = 12.sp,
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.usual_medium))
        )
    }
}

@Preview
@Composable
fun NewFeatureLabelPreview() {
    MenuLabel(colorResource(id = R.color.colorAccent), stringResource(id = R.string.new_label))
}