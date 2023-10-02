package org.permanent.permanent.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R

@Composable
fun EditMetadataScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
//
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
//    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
//    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
//    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
//    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
//    val smallTextSize = 11.sp
//    val subTitleTextSize = 16.sp
//    val titleTextSize = 19.sp
//
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlueColor)
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) { }
//        Text(
//            text = title.uppercase(),
//            fontSize = smallTextSize,
//            color = primaryColor,
//            fontFamily = boldFont,
//            modifier = Modifier
//                .align(Alignment.Start)
//                .padding(top = 36.dp)
//        )
//        Spacer(modifier = Modifier.height(10.dp))
//        Text(
//            text = subtitle,
//            fontSize = titleTextSize,
//            color = primaryColor,
//            fontFamily = semiBoldFont,
//            modifier = Modifier.align(Alignment.Start)
//        )
//        Spacer(modifier = Modifier.height(40.dp))
//        LegacyContactCard(
//            name,
//            email,
//            cardTitle,
//            cardSubtitle,
//            cardButtonName,
//            whiteColor,
//            primaryColor,
//            semiBoldFont,
//            blackColor,
//            regularFont,
//            boldFont,
//            openAddEditScreen
//        )
//        Spacer(modifier = Modifier.weight(1.0f))
//        Button(modifier = Modifier
//            .fillMaxWidth()
//            .height(48.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
//            shape = RoundedCornerShape(8.dp),
//            onClick = { openLegacyScreen() }) {
//            Row(
//                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = stringResource(R.string.button_go_to_legacy_planning),
//                    fontSize = subTitleTextSize,
//                    fontFamily = regularFont,
//                )
//                Spacer(modifier = Modifier.weight(1.0f))
//                Image(
//                    painter = painterResource(id = R.drawable.ic_arrow_next_white),
//                    contentDescription = "Account add",
//                    modifier = Modifier.size(16.dp)
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(36.dp))
//    }
}