package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R

@Composable
fun TwoStepDisabledScreen(
    isTablet: Boolean,
    onAddTwoStepVerificationClick: () -> Unit
) {
    if (isTablet) {
        TabletBody(onAddTwoStepVerificationClick)
    } else {
        PhoneBody(onAddTwoStepVerificationClick)
    }
}

@Composable
private fun TabletBody(
    onAddTwoStepVerificationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.superLightBlue))
            .padding(horizontal = 128.dp, vertical = 64.dp)
    ) {
        // Title Text
        Text(
            text = stringResource(id = R.string.two_step_verification),
            color = colorResource(id = R.color.colorPrimary),
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = FontFamily(Font(R.font.usual_medium)),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Top banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.errorLight),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_lock_open_red),
                contentDescription = "Next",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(id = R.string.two_step_verification_is))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" " + stringResource(id = R.string.disabled) + ".")
                    }
                },
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = colorResource(id = R.color.darkRed),
                fontFamily = FontFamily(Font(R.font.usual_regular))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Description Text
        Text(
            text = stringResource(id = R.string.two_step_verification_description),
            color = colorResource(id = R.color.colorPrimary),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily(Font(R.font.usual_medium)),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Button
        Button(
            onClick = onAddTwoStepVerificationClick,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.colorPrimary)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.add_two_step_verification),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium))
            )
        }
    }
}

@Composable
private fun PhoneBody(
    onAddTwoStepVerificationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.superLightBlue))
    ) {
        // Top banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.errorLight))
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_lock_open_red),
                contentDescription = "Next",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = buildAnnotatedString {
                    append(stringResource(id = R.string.two_step_verification_is))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" " + stringResource(id = R.string.disabled) + ".")
                    }
                },
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = colorResource(id = R.color.darkRed),
                fontFamily = FontFamily(Font(R.font.usual_regular))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Description Text
        Text(
            text = stringResource(id = R.string.two_step_verification_description),
            color = colorResource(id = R.color.colorPrimary),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily(Font(R.font.usual_medium)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        Button(
            onClick = onAddTwoStepVerificationClick,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.colorPrimary)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.add_two_step_verification),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium))
            )
        }
    }
}