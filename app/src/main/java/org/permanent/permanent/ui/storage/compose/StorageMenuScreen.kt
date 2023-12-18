package org.permanent.permanent.ui.storage.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.ui.bytesToCustomHumanReadableString
import org.permanent.permanent.viewmodels.StorageMenuViewModel

@Composable
fun StorageMenuScreen(viewModel: StorageMenuViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val purpleColor = Color(ContextCompat.getColor(context, R.color.barneyPurple))
    val redColor = Color(ContextCompat.getColor(context, R.color.red))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val smallTextSize = 14.sp

    val spaceTotalBytes by viewModel.getSpaceTotal().observeAsState(initial = 0L)
    val spaceLeftBytes by viewModel.getSpaceLeft().observeAsState(initial = 0L)
    val spaceUsedPercentage by viewModel.getSpaceUsedPercentage().observeAsState(initial = 0)

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_plus_light_grey),
                contentDescription = "Next",
                colorFilter = ColorFilter.tint(whiteColor),
                modifier = Modifier.size(16.dp)
            )
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .weight(1.0f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Add storage",
                    fontSize = smallTextSize,
                    color = Color.Black,
                    fontFamily = semiboldFont
                )
                Text(
                    text = "Increase your space easily by adding more storage.",
                    fontSize = smallTextSize,
                    color = Color.Black,
                    fontFamily = regularFont
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_select_grey),
                contentDescription = "Next",
                colorFilter = ColorFilter.tint(whiteColor),
                modifier = Modifier.size(24.dp)
            )

//            Divider(modifier = Modifier.padding(vertical = 24.dp))
        }
    }
}