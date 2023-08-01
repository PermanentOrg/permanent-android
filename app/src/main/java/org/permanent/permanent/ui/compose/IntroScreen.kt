package org.permanent.permanent.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun IntroScreen(navigateToDesignateContactScreen: () -> Unit, onCloseScreen: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val whiteTransparentColor = Color(ContextCompat.getColor(context, R.color.whiteTransparent))
    val transparentColor = Color.Transparent
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val smallTextSize = 15.sp
    val titleTextSize = 25.sp
    val subTitleTextSize = 19.sp

    val alignLeft = remember { mutableStateOf(false) }
    val showAdditionalText = remember { mutableStateOf(false) }
    val imageSize = if (showAdditionalText.value) 52.dp else 136.dp
    val spacerHeight = if (showAdditionalText.value) 52.dp else 200.dp
    val descriptionText =
        stringResource(if (showAdditionalText.value) R.string.set_up_your_legacy_plan_description_long else R.string.set_up_your_legacy_plan_description_short)
    val tellMeMoreButtonText =
        stringResource(if (showAdditionalText.value) R.string.ill_do_this_later else R.string.tell_me_more_about_legacy_planning)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryColor)
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_legacy_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(imageSize)
        )
        Spacer(modifier = Modifier.height(44.dp))
        Text(
            text = stringResource(R.string.set_up_your_legacy_plan_title),
            fontSize = titleTextSize,
            color = whiteColor,
            fontFamily = regularFont,
            modifier = Modifier.align(if (alignLeft.value) Alignment.Start else Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = descriptionText,
            fontSize = smallTextSize,
            color = whiteTransparentColor,
            fontFamily = regularFont
        )
        if (showAdditionalText.value) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.account_legacy_plan_title),
                fontSize = subTitleTextSize,
                color = whiteColor,
                fontFamily = regularFont,
                modifier = Modifier.align(if (alignLeft.value) Alignment.Start else Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.account_legacy_plan_description),
                fontSize = smallTextSize,
                color = whiteTransparentColor,
                fontFamily = regularFont,
                modifier = Modifier.align(if (alignLeft.value) Alignment.Start else Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.archive_legacy_plans_title),
                fontSize = subTitleTextSize,
                color = whiteColor,
                fontFamily = regularFont,
                modifier = Modifier.align(if (alignLeft.value) Alignment.Start else Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.archive_legacy_plans_description),
                fontSize = smallTextSize,
                color = whiteTransparentColor,
                fontFamily = regularFont,
                modifier = Modifier.align(if (alignLeft.value) Alignment.Start else Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(spacerHeight))
        Button(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = whiteColor,
                contentColor = primaryColor
            ),
            shape = RoundedCornerShape(8.dp),
            onClick = { navigateToDesignateContactScreen() }
        ) {
            Text(
                text = stringResource(R.string.set_up_my_account_plan_now),
                fontSize = smallTextSize,
                fontFamily = boldFont
            )
        }
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = transparentColor),
            onClick = {
                showAdditionalText.value = true
                alignLeft.value = true
                if (tellMeMoreButtonText == context.getString(R.string.ill_do_this_later)) {
                    onCloseScreen()
                }
            }
        ) {
            Text(
                text = tellMeMoreButtonText,
                fontSize = smallTextSize,
                fontFamily = regularFont,
            )
        }
        Spacer(modifier = Modifier.height(36.dp))
    }
}