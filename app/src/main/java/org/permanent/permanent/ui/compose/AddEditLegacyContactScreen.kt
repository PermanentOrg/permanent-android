@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.permanent.permanent.R

@Composable
fun AddEditLegacyContactScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val elementsSpacing = 32.dp
    val superSmallTextSize = 10.sp
    val smallTextSize = 13.sp
    val titleTextSize = 15.sp

    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteColor)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomSheetHeader()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = elementsSpacing),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.designate_account_legacy_contact),
                fontSize = titleTextSize,
                color = primaryColor,
                fontFamily = boldFont,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = elementsSpacing)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.designate_account_legacy_contact_description),
                fontSize = smallTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(elementsSpacing))
            TextField(
                value = contactName,
                onValueChange = { value -> contactName = value },
                label = { Text(text = stringResource(R.string.contact_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_account_empty_multicolor),
                        contentDescription = "Contact Name Icon"
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = lightBlueColor,
                    unfocusedContainerColor = lightBlueColor,
                    focusedIndicatorColor = lightBlueColor,
                    unfocusedIndicatorColor = lightBlueColor,
                    focusedLabelColor = lightGreyColor,
                    unfocusedLabelColor = lightGreyColor
                )
            )
            TextField(
                value = contactEmail,
                onValueChange = { value -> contactEmail = value },
                label = { Text(text = stringResource(R.string.contact_email_address)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                leadingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_email_multicolor),
                        contentDescription = "Contact Email Icon"
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = lightBlueColor,
                    unfocusedContainerColor = lightBlueColor,
                    focusedIndicatorColor = lightBlueColor,
                    unfocusedIndicatorColor = lightBlueColor,
                    focusedLabelColor = lightGreyColor,
                    unfocusedLabelColor = lightGreyColor
                )
            )
            Divider(modifier = Modifier.padding(vertical = elementsSpacing))
            Text(
                text = stringResource(R.string.note_title).uppercase(),
                fontSize = superSmallTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = stringResource(R.string.note_description),
                fontSize = smallTextSize,
                color = blackColor,
                fontFamily = regularFont,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(elementsSpacing))
}

@Composable
private fun BottomSheetHeader() {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val subTitleTextSize = 16.sp
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.button_cancel),
            fontSize = subTitleTextSize,
            fontFamily = regularFont,
            color = whiteColor
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            text = stringResource(R.string.legacy_contact),
            fontSize = subTitleTextSize,
            fontFamily = semiBoldFont,
            color = whiteColor
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            text = stringResource(R.string.button_save),
            fontSize = subTitleTextSize,
            fontFamily = regularFont,
            color = whiteColor
        )
    }
}