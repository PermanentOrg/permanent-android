@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.CustomDropdown
import org.permanent.permanent.ui.composeComponents.SmallTextAndIconButton

@Composable
fun TypeSelectionPage(isTablet: Boolean, pagerState: PagerState) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

//    if (isTablet) {
//        TabletBody(whiteColor, regularFont)
//    } else {
    PhoneBody(whiteColor, regularFont, coroutineScope, pagerState)
//    }
}

@Composable
private fun PhoneBody(
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val titleText = stringResource(id = R.string.create_your_first_archive_title)
        val boldedWord = "archive"
        val start = titleText.indexOf(boldedWord)
        val spanStyles = listOf(
            AnnotatedString.Range(
                SpanStyle(fontWeight = FontWeight.Bold),
                start = start,
                end = start + boldedWord.length
            )
        )

        Text(
            text = AnnotatedString(text = titleText, spanStyles = spanStyles),
            fontSize = 32.sp,
            lineHeight = 48.sp,
            color = whiteColor,
            fontFamily = regularFont
        )

        Text(
            text = stringResource(id = R.string.create_your_first_archive_description),
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = whiteColor,
            fontFamily = regularFont
        )

        CustomDropdown()

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SmallTextAndIconButton(
                    ButtonColor.TRANSPARENT,
                    text = stringResource(id = R.string.back),
                    icon = painterResource(id = R.drawable.ic_arrow_back_rounded_white),
                    iconAlignment = ButtonIconAlignment.START
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SmallTextAndIconButton(
                    ButtonColor.LIGHT,
                    text = stringResource(id = R.string.next)
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
            }
        }
    }
}