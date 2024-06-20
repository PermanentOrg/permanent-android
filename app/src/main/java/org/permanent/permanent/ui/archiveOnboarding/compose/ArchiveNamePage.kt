@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.SmallTextAndIconButton
import org.permanent.permanent.ui.composeComponents.TextAndIconButton


@Composable
fun ArchiveNamePage(
    isTablet: Boolean,
    pagerState: PagerState,
    newArchive: NewArchive
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val blue400Color = Color(ContextCompat.getColor(context, R.color.blue400))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    if (isTablet) {
        TabletBody(whiteColor, blue400Color, regularFont, coroutineScope, pagerState, newArchive)
    } else {
        PhoneBody(whiteColor, blue400Color, regularFont, coroutineScope, pagerState, newArchive)
    }
}

@Composable
private fun TabletBody(
    whiteColor: Color,
    blue400Color: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive
) {
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = ""
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        horizontalArrangement = Arrangement.spacedBy(64.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val titleText =
                stringResource(id = R.string.create_your_archive_title, newArchive.typeName)
            val boldedWord = newArchive.typeName
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
                fontSize = 56.sp,
                lineHeight = 72.sp,
                color = whiteColor,
                fontFamily = regularFont
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(id = R.string.create_your_archive_description),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = whiteColor,
                fontFamily = regularFont
            )

            Spacer(modifier = Modifier.height(64.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(1.dp, blue400Color, RoundedCornerShape(10.dp)),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = stringResource(id = R.string.the),
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    color = Color.White,
                    fontFamily = regularFont
                )

                TextField(
                    value = textFieldValueState,
                    onValueChange = { value -> textFieldValueState = value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.name_dots),
                            color = blue400Color,
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            fontFamily = regularFont
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        fontFamily = regularFont,
                        fontWeight = FontWeight(600)
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = blue400Color
                    )
                )
                Text(
                    modifier = Modifier.padding(end = 16.dp),
                    text = stringResource(id = R.string.archive),
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    color = Color.White,
                    fontFamily = regularFont
                )
            }

            Spacer(modifier = Modifier.weight(1.0f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Box(
                    modifier = Modifier.width(120.dp)
                ) {
                    SmallTextAndIconButton(
                        ButtonColor.TRANSPARENT,
                        text = stringResource(id = R.string.back),
                        icon = painterResource(id = R.drawable.ic_arrow_back_rounded_white),
                        iconAlignment = ButtonIconAlignment.START
                    ) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_TYPE_PAGE.value)
                        }
                    }
                }

                TextAndIconButton(
                    ButtonColor.LIGHT,
                    text = stringResource(id = R.string.create_the_archive),
                    showButtonEnabled = textFieldValueState.text.isNotEmpty()
                ) {
                    if (textFieldValueState.text.isNotEmpty()) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(OnboardingPage.GOALS_PAGE.value)
                        }
                        newArchive.name = textFieldValueState.text
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneBody(
    whiteColor: Color,
    blue400Color: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive
) {
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = ""
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val titleText = stringResource(id = R.string.create_your_archive_title, newArchive.typeName)
        val boldedWord = newArchive.typeName
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
            text = stringResource(id = R.string.create_your_archive_description),
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = whiteColor,
            fontFamily = regularFont
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, blue400Color, RoundedCornerShape(10.dp)),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = stringResource(id = R.string.the),
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = regularFont
            )

            TextField(
                value = textFieldValueState,
                onValueChange = { value -> textFieldValueState = value },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.name_dots),
                        color = blue400Color,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = regularFont
                    )
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = regularFont,
                    fontWeight = FontWeight(600),
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = blue400Color
                )
            )

            Text(
                modifier = Modifier.padding(end = 16.dp),
                text = stringResource(id = R.string.archive),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontFamily = regularFont
            )
        }

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
                    buttonColor = ButtonColor.TRANSPARENT,
                    text = stringResource(id = R.string.back),
                    icon = painterResource(id = R.drawable.ic_arrow_back_rounded_white),
                    iconAlignment = ButtonIconAlignment.START
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_TYPE_PAGE.value)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SmallTextAndIconButton(
                    buttonColor = ButtonColor.LIGHT,
                    text = stringResource(id = R.string.next),
                    showButtonEnabled = textFieldValueState.text.isNotEmpty()
                ) {
                    if (textFieldValueState.text.isNotEmpty()) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(OnboardingPage.GOALS_PAGE.value)
                        }
                        newArchive.name = textFieldValueState.text
                    }
                }
            }
        }
    }
}