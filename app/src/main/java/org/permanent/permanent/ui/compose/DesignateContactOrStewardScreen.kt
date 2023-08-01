package org.permanent.permanent.ui.compose

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
fun DesignateContactOrStewardScreen() {
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
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = stringResource(R.string.designate_a_legacy_contact).uppercase(),
            fontSize = smallTextSize,
            color = primaryColor,
            fontFamily = boldFont,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.designate_contact_title),
            fontSize = titleTextSize,
            color = primaryColor,
            fontFamily = semiBoldFont,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(40.dp))
        LegacyContactCard(whiteColor, primaryColor, semiBoldFont, blackColor, regularFont, boldFont)
        Spacer(modifier = Modifier.height(300.dp))
        Button(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(8.dp),
            onClick = {
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.button_go_to_legacy_planning),
                    fontSize = subTitleTextSize,
                    fontFamily = regularFont,
                )
                Spacer(modifier = Modifier.width(116.dp))
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
    cardColor: Color,
    textColor: Color,
    semiBoldFont: FontFamily,
    blackColor: Color,
    regularFont: FontFamily,
    boldFont: FontFamily
) {
    val mediumTextSize = 14.sp
    val subTitleTextSize = 16.sp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_account_filled_multicolor),
                    contentDescription = "Account logo",
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.a_trusted_legacy_contact_title),
                    fontSize = subTitleTextSize,
                    color = textColor,
                    fontFamily = semiBoldFont
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.a_trusted_legacy_contact_description),
                fontSize = mediumTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier.align(Alignment.Start)
            )
            Divider(modifier = Modifier.padding(vertical = 24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_legacy_contact),
                    fontSize = mediumTextSize,
                    color = textColor,
                    fontFamily = boldFont
                )
                Spacer(modifier = Modifier.width(132.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_account_add_primary),
                    contentDescription = "Account add",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}