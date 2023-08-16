package org.permanent.permanent.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun DesignateContactOrStewardScreen(
    name: String?,
    email: String?,
    title: String,
    subtitle: String,
    cardTitle: String,
    cardSubtitle: String,
    cardButtonName: String,
    openAddEditScreen: () -> Unit,
    openLegacyScreen: () -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val smallTextSize = 11.sp
    val subTitleTextSize = 16.sp
    val titleTextSize = 19.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlueColor)
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            fontSize = smallTextSize,
            color = primaryColor,
            fontFamily = boldFont,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = subtitle,
            fontSize = titleTextSize,
            color = primaryColor,
            fontFamily = semiBoldFont,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(40.dp))
        LegacyContactCard(
            name,
            email,
            cardTitle,
            cardSubtitle,
            cardButtonName,
            whiteColor,
            primaryColor,
            semiBoldFont,
            blackColor,
            regularFont,
            boldFont,
            openAddEditScreen
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Button(modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(8.dp),
            onClick = { openLegacyScreen() }) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.button_go_to_legacy_planning),
                    fontSize = subTitleTextSize,
                    fontFamily = regularFont,
                )
                Spacer(modifier = Modifier.weight(1.0f))
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_next_white),
                    contentDescription = "Account add",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun LegacyContactCard(
    name: String?,
    email: String?,
    cardTitle: String,
    cardSubtitle: String,
    cardButtonName: String,
    cardColor: Color,
    textColor: Color,
    semiBoldFont: FontFamily,
    blackColor: Color,
    regularFont: FontFamily,
    boldFont: FontFamily,
    openAddEditScreen: () -> Unit
) {
    val mediumTextSize = 14.sp
    val subTitleTextSize = 16.sp
    val context = LocalContext.current
    val middleGray = Color(ContextCompat.getColor(context, R.color.middleGrey))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_account_filled_multicolor),
                    contentDescription = "Account logo",
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cardTitle,
                    fontSize = subTitleTextSize,
                    color = textColor,
                    fontFamily = semiBoldFont
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = cardSubtitle,
                fontSize = mediumTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier.align(Alignment.Start)
            )
            Divider(modifier = Modifier.padding(vertical = 24.dp))
            name?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openAddEditScreen() },
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1.0f, fill = false)) {
                        Text(
                            text = it,
                            fontSize = mediumTextSize,
                            color = textColor,
                            fontFamily = boldFont
                        )
                        email?.let {
                            Text(
                                text = it,
                                fontSize = mediumTextSize,
                                color = middleGray,
                                fontFamily = regularFont
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_edit_primary),
                        contentDescription = "Edit steward",
                        modifier = Modifier
                            .size(16.dp)
                    )
                }
            } ?: run {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openAddEditScreen() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cardButtonName,
                        fontSize = mediumTextSize,
                        color = textColor,
                        fontFamily = boldFont
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    Image(
                        painter = painterResource(id = R.drawable.ic_account_add_primary),
                        contentDescription = "Account add",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}