package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.models.Record
import org.permanent.permanent.models.Tag
import org.permanent.permanent.viewmodels.EditMetadataViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditMetadataScreen(
    viewModel: EditMetadataViewModel,
    openNewTagScreen: (tagsOfSelectedRecords: ArrayList<Tag>) -> Unit,
    openEditFileNamesScreen: (records: MutableList<Record>) -> Unit,
    openDateAndTimeScreen: (records: MutableList<Record>) -> Unit,
    openLocationScreen: (records: MutableList<Record>) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val redColor = Color(ContextCompat.getColor(context, R.color.red))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val smallTextSize = 14.sp
    val subTitleTextSize = 16.sp

    val records by remember { mutableStateOf(viewModel.getRecords()) }
    val firstRecordThumb by remember { mutableStateOf(records[0].thumbURL200) }
    val titleString = stringResource(R.string.edit_files_metadata_title, records.size)
    val headerTitle by remember { mutableStateOf(titleString) }
    var inputDescription by remember { mutableStateOf("") }
    val someFilesHaveDescription by viewModel.getSomeFilesHaveDescription().observeAsState()
    val allTags by viewModel.getTagsOfSelectedRecords().observeAsState()
    val focusRequester = remember { FocusRequester() }
    val errorMessage by viewModel.showError.observeAsState()
    val showApplyAllToSelection by viewModel.showApplyAllToSelection.observeAsState()
    val isBusy by viewModel.getIsBusy().observeAsState()

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

        DescriptionView(
            blackColor,
            regularFont,
            subTitleTextSize,
            inputDescription,
            focusRequester,
            whiteColor,
            lightGreyColor,
            someFilesHaveDescription,
            redColor,
            smallTextSize
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tag),
                    contentDescription = "Description",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(lightGreyColor)
                )
                Text(
                    text = stringResource(R.string.edit_files_metadata_tags),
                    color = blackColor,
                    fontFamily = regularFont,
                    fontSize = subTitleTextSize
                )
            }
            if (showApplyAllToSelection == true) {
                Row(modifier = Modifier
                    .clickable { viewModel.onApplyAllTagsToSelectionClick() }
                    .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.edit_files_metadata_apply_all_to_selection),
                        color = primaryColor,
                        fontFamily = semiboldFont,
                        fontSize = subTitleTextSize
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_done_white),
                        contentDescription = "Description",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(primaryColor)
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            allTags?.let { allTagsValue ->
                for (tag in allTagsValue) {
                    TagView(
                        text = tag.name,
                        isSelected = tag.isSelected.observeAsState(),
                        onTagClick = { viewModel.onTagClick(tag) }
                    ) { viewModel.onTagRemoveClick(tag) }
                }
            }
            NewTagView {
                val tagsOfSelectedRecords = arrayListOf<Tag>()
                viewModel.getTagsOfSelectedRecords().value?.toList()
                    ?.let { tagsOfSelectedRecords.addAll(it) }

                openNewTagScreen(tagsOfSelectedRecords)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        FilesMenuView(icon = R.drawable.ic_edit_name,
            title = stringResource(id = R.string.file_names),
            actionTitle = stringResource(id = R.string.modify)) {
            openEditFileNamesScreen(viewModel.getRecords())
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        FilesMenuView(icon = R.drawable.ic_edit_name,
            title = stringResource(id = R.string.locations),
            actionTitle = stringResource(id = R.string.menu_toolbar_public_add)) {
            openLocationScreen(viewModel.getRecords())
        }
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

@Composable
private fun DescriptionView(
    blackColor: Color,
    regularFont: FontFamily,
    subTitleTextSize: TextUnit,
    inputDescription: String,
    focusRequester: FocusRequester,
    whiteColor: Color,
    lightGreyColor: Color,
    someFilesHaveDescription: Boolean?,
    redColor: Color,
    smallTextSize: TextUnit
) {
    var description = inputDescription
    Row(
        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)
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
        value = description,
        onValueChange = { value ->
            description = value
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
}

@Composable
private fun FilesMenuView(
    icon: Int,
    title: String,
    actionTitle: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val blue900 = Color(ContextCompat.getColor(context, R.color.blue900))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))

    Row(
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Description",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(lightGreyColor)
            )
            Text(
                text = title,
                color = blackColor,
                fontFamily = regularFont,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.weight(1.0f))

        Row{
            Text(
                text = actionTitle,
                color = blue900,
                fontFamily = semiBoldFont,
                fontSize = 15.sp
            )

            Image(
                painter = painterResource(id = R.drawable.ic_arrow_select_grey),
                contentDescription = "Description",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(blue900))
        }
    }
}