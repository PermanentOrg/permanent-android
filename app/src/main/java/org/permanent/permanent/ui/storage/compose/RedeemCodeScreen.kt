package org.permanent.permanent.ui.storage.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.RedeemCodeViewModel

@Composable
fun RedeemCodeScreen(viewModel: RedeemCodeViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val primaryColor200 = Color(ContextCompat.getColor(context, R.color.colorPrimary200))
    val error200Color = Color(ContextCompat.getColor(context, R.color.error200))
    val redColor = Color(ContextCompat.getColor(context, R.color.red))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val lighterGreyColor = Color(ContextCompat.getColor(context, R.color.lighterGrey))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))

    val showError by viewModel.showError.observeAsState(initial = "")
    val showButtonEnabled by viewModel.getShowButtonEnabled().observeAsState(initial = false)
    val isBusy by viewModel.getIsBusy().observeAsState(initial = false)

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

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(id = R.string.redeem_code_title),
                fontSize = 16.sp,
                color = primaryColor,
                fontFamily = boldFont
            )

            Text(
                text = stringResource(id = R.string.redeem_code_explanation),
                fontSize = 14.sp,
                color = primaryColor,
                fontFamily = semiboldFont
            )
        }

        if (isBusy == true) {
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(32.dp),
                    color = primaryColor,
                    trackColor = MaterialTheme.colorScheme.secondary,
                )
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = stringResource(R.string.enter_code).uppercase(),
            fontSize = 10.sp,
            color = primaryColor,
            fontFamily = regularFont
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = viewModel.code,
            onValueChange = { value -> viewModel.updateEnteredCode(value) },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (showError.isNullOrEmpty()) lighterGreyColor else error200Color,
                    RoundedCornerShape(10.dp)
                ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = whiteColor,
                unfocusedContainerColor = whiteColor,
                focusedIndicatorColor = whiteColor,
                unfocusedIndicatorColor = whiteColor,
                focusedLabelColor = lightGreyColor,
                unfocusedLabelColor = lightGreyColor
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (showButtonEnabled) primaryColor else primaryColor200),
            shape = RoundedCornerShape(8.dp),
            onClick = { if (showButtonEnabled) viewModel.onRedeemButtonClick() }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.redeem),
                    fontSize = 16.sp,
                    fontFamily = regularFont,
                )
            }
        }
    }
}