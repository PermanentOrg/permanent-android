package org.permanent.permanent.ui.compose.gifting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.bytesToCustomHumanReadableString
import org.permanent.permanent.viewmodels.GiftStorageViewModel

@Composable
fun GiftStorageScreen(viewModel: GiftStorageViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val purpleColor = Color(ContextCompat.getColor(context, R.color.barneyPurple))
    val redColor = Color(ContextCompat.getColor(context, R.color.red))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val lighterGreyColor = Color(ContextCompat.getColor(context, R.color.lighterGrey))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val italicFont = FontFamily(Font(R.font.open_sans_italic_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val subTitleTextSize = 16.sp
    val smallTextSize = 14.sp
    val smallerTextSize = 12.sp
    val smallestTextSize = 11.sp

    val spaceTotalBytes by viewModel.getSpaceTotal().observeAsState(initial = 0L)
    val spaceLeftBytes by viewModel.getSpaceLeft().observeAsState(initial = 0L)
    val spaceUsedPercentage by viewModel.getSpaceUsedPercentage().observeAsState(initial = 0)
    val emailNr by viewModel.getEmailsNr().observeAsState(initial = 0)
    val giftGB by viewModel.getGiftGB().observeAsState(initial = 0)
    val giftBytes by viewModel.getGiftBytes().observeAsState(initial = 0)
    val showInsufficientStorageText by viewModel.getShowInsufficientStorageText()
        .observeAsState(initial = false)
    var note by remember { mutableStateOf(viewModel.getNote()) }
    val errorMessage by viewModel.showError.observeAsState()
//    val isBusy by viewModel.getIsBusy().observeAsState()

    val coroutineScope = rememberCoroutineScope()
    val snackbarEventFlow = remember { MutableSharedFlow<String>() }
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlueColor)
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            progress = spaceUsedPercentage.toFloat() / 100,
            color = purpleColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = spaceTotalBytes?.let {
                    bytesToCustomHumanReadableString(it, false)
                } + " Storage",
                fontSize = smallTextSize,
                color = primaryColor,
                fontFamily = semiboldFont
            )
            Text(
                text = spaceLeftBytes?.let { bytesToCustomHumanReadableString(it, true) } + " free",
                fontSize = smallTextSize,
                color = primaryColor,
                fontFamily = semiboldFont
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.gift),
            fontSize = smallerTextSize,
            color = primaryColor,
            fontFamily = semiboldFont
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            border = BorderStroke(
                0.dp,
                if (showInsufficientStorageText) redColor else lightGreyColor
            ),
            colors = CardDefaults.cardColors(containerColor = whiteColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = giftGB.toString(),
                        fontSize = smallTextSize,
                        color = primaryColor,
                        fontFamily = semiboldFont
                    )
                    Text(
                        text = stringResource(R.string.gb_recipient),
                        fontSize = smallestTextSize,
                        color = primaryColor,
                        fontFamily = semiboldFont
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_minus_light_grey),
                        contentDescription = "Minus",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.onMinusBtnClick() },
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_plus_light_grey),
                        contentDescription = "Plus",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.onPlusBtnClick() },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showInsufficientStorageText) {
            Text(
                text = stringResource(
                    R.string.insufficient_storage,
                    bytesToCustomHumanReadableString(emailNr * giftBytes - spaceLeftBytes, true)
                ),
                fontSize = smallestTextSize,
                color = redColor,
                fontFamily = italicFont
            )
        }

        if (giftGB > 0 && emailNr * giftBytes <= spaceLeftBytes) {
            Text(
                text = stringResource(
                    R.string.total_gifted_forecasted_remaining,
                    bytesToCustomHumanReadableString(emailNr * giftBytes, true),
                    bytesToCustomHumanReadableString(spaceLeftBytes - emailNr * giftBytes, true)
                ),
                fontSize = smallestTextSize,
                color = primaryColor,
                fontFamily = italicFont
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.note_to_recipients),
                fontSize = smallerTextSize,
                color = primaryColor,
                fontFamily = semiboldFont
            )
            Text(
                text = stringResource(R.string.optional),
                fontSize = smallerTextSize,
                color = lightGreyColor,
                fontFamily = semiboldFont
            )
        }

        TextField(
            value = note,
            onValueChange = { value -> note = value },
            label = { Text(text = stringResource(R.string.your_text_here)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .border(1.dp, lighterGreyColor, RoundedCornerShape(10.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = whiteColor,
                unfocusedContainerColor = whiteColor,
                focusedIndicatorColor = whiteColor,
                unfocusedIndicatorColor = whiteColor,
                focusedLabelColor = lightGreyColor,
                unfocusedLabelColor = lightGreyColor
            )
        )

//        if (isBusy == true) {
//            CircularProgressIndicator(
//                modifier = Modifier.width(48.dp),
//                color = MaterialTheme.colorScheme.surfaceVariant,
//                trackColor = MaterialTheme.colorScheme.secondary,
//            )
//        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    LaunchedEffect(snackbarEventFlow) {
        snackbarEventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}