package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
fun CustomCheckbox(
    isTablet: Boolean = false,
    text: String,
    checkedState: MutableState<Boolean>
) {
    val context = LocalContext.current
    val whiteTransparentColor =
        Color(ContextCompat.getColor(context, R.color.whiteSuperExtraTransparent))
    val blue900Color = Color(ContextCompat.getColor(context, R.color.blue900))
    val barneyPurpleColor = Color(ContextCompat.getColor(context, R.color.barneyPurple))
    val barneyPurpleLightColor = Color(ContextCompat.getColor(context, R.color.barneyPurpleLight))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Row(
        modifier = Modifier
            .height(if (isTablet) 116.dp else 96.dp)
            .fillMaxWidth()
            .border(1.dp, whiteTransparentColor, RoundedCornerShape(10.dp))
            .background(
                if (checkedState.value) Brush.horizontalGradient(
                    listOf(
                        barneyPurpleLightColor,
                        barneyPurpleColor
                    )
                ) else if (isTablet) Brush.horizontalGradient(
                    listOf(
                        blue900Color,
                        blue900Color
                    )
                )
                else
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    ), RoundedCornerShape(10.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            modifier = Modifier.padding(start = 24.dp),
            checked = checkedState.value,
            onCheckedChange = { checkedState.value = it },
            colors = CheckboxDefaults.colors(
                uncheckedColor = Color.White,
                checkedColor = Color.White,
                checkmarkColor = barneyPurpleLightColor
            )
        )

        Text(
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp, end = 24.dp),
            text = text,
            fontSize = if (isTablet) 18.sp else 14.sp,
            lineHeight = if (isTablet) 30.sp else 24.sp,
            color = Color.White,
            fontFamily = regularFont
        )
    }
}

@Preview
@Composable
fun CustomCheckboxPreview() {
    val checkedState = remember { mutableStateOf(false) }

    CustomCheckbox(
        isTablet = false,
        text = stringResource(id = R.string.goals_capture),
        checkedState = checkedState
    )
}