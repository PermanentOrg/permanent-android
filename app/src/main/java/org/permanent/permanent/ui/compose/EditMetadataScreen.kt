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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditMetadataViewModel

@Composable
fun EditMetadataScreen(viewModel: EditMetadataViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val redColor = Color(ContextCompat.getColor(context, R.color.red))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
//    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
//    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val smallTextSize = 14.sp
    val subTitleTextSize = 16.sp
//    val titleTextSize = 19.sp

    val records by remember { mutableStateOf(viewModel.getRecords()) }
    val firstRecordThumb by remember { mutableStateOf(records[0].thumbURL200) }
    val titleString = stringResource(R.string.edit_files_metadata_title, records.size)
    val headerTitle by remember { mutableStateOf(titleString) }
    var inputDescription by remember { mutableStateOf("") }
    val someFilesHaveDescription by viewModel.getSomeFilesHaveDescription().observeAsState()
    val focusRequester = remember { FocusRequester() }
    val errorMessage by viewModel.showError.observeAsState()

    val coroutineScope = rememberCoroutineScope()
    val snackbarEventFlow = remember { MutableSharedFlow<String>() }
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlueColor)
            .padding(24.dp)
            .verticalScroll(scrollState)
            .clickable {
                viewModel.applyNewDescriptionToAllRecords(inputDescription)
            },
        verticalArrangement = Arrangement.Top
    ) {
        Header(iconURL = firstRecordThumb, titleText = headerTitle)

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_description_edit_metadata),
                contentDescription = "Description",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.edit_files_metadata_description),
                color = blackColor,
                fontFamily = regularFont,
                fontSize = subTitleTextSize
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = inputDescription,
            onValueChange = { value ->
                inputDescription = value
            },
            label = { Text(text = stringResource(id = R.string.edit_files_metadata_description_hint)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = whiteColor,
                unfocusedContainerColor = whiteColor,
                focusedIndicatorColor = whiteColor,
                unfocusedIndicatorColor = whiteColor,
                focusedLabelColor = lightGreyColor,
                unfocusedLabelColor = lightGreyColor
            )
        )

        if (someFilesHaveDescription == true) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.edit_files_metadata_description_warning),
                color = redColor,
                fontFamily = regularFont,
                fontSize = smallTextSize
            )
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp))
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

@Composable
private fun Header(
    iconURL: String?, titleText: String?
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val semiboldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val subTitleTextSize = 16.sp

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = iconURL,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (titleText != null) {
                Text(
                    text = titleText,
                    color = primaryColor,
                    fontFamily = semiboldFont,
                    fontSize = subTitleTextSize
                )
            }
        }
    }
}
