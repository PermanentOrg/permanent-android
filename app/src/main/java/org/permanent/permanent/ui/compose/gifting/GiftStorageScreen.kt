package org.permanent.permanent.ui.compose.gifting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val smallTextSize = 14.sp
    val subTitleTextSize = 16.sp

//    val headerTitle by remember { mutableStateOf(titleString) }
    val spaceTotal by viewModel.getSpaceTotal().observeAsState()
    val spaceLeft by viewModel.getSpaceLeft().observeAsState()
    val spaceUsedPercentage by viewModel.getSpaceUsedPercentage().observeAsState()
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
        spaceUsedPercentage?.toFloat()?.let {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(12.dp),
                progress = it/100,
                color = purpleColor
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = spaceTotal?.let { bytesToCustomHumanReadableString(it, false) } + " Storage",
                fontSize = smallTextSize,
                color = primaryColor,
                fontFamily = semiboldFont
            )
            Text(
                text = spaceLeft?.let { bytesToCustomHumanReadableString(it, true) } + " free",
                fontSize = smallTextSize,
                color = primaryColor,
                fontFamily = semiboldFont
            )
        }

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