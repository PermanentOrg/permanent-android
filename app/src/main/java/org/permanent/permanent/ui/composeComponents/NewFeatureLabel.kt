package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun NewFeatureLabel(
) {
    val context = LocalContext.current

    val accentColor = Color(ContextCompat.getColor(context, R.color.colorAccent))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))

    Column(
        modifier = Modifier
            .background(accentColor, RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(id = R.string.new_label).uppercase(),
            fontSize = 12.sp,
            color = whiteColor,
            fontFamily = semiboldFont
        )
    }
}

@Preview
@Composable
fun NewFeatureLabelPreview() {
    NewFeatureLabel()
}