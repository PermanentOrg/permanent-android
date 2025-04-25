package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
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
import org.permanent.permanent.viewmodels.NewTagViewModel


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTagScreen(viewModel: NewTagViewModel, onCancelBtnClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val blackColor = Color(ContextCompat.getColor(context, R.color.black))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.blue25))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val lightGreyColor = Color(ContextCompat.getColor(context, R.color.lightGrey))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val semiBoldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val superSmallTextSize = 10.sp

    val recentTags by viewModel.getRecentTags().observeAsState(initial = mutableListOf())
    val errorMessage by viewModel.showError.observeAsState()
    val isBusy by viewModel.getIsBusy().observeAsState(initial = false)

    val coroutineScope = rememberCoroutineScope()
    val snackbarEventFlow = remember { MutableSharedFlow<String>() }
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteColor),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BottomSheetHeader(
            painterResource(id = R.drawable.ic_tag), screenTitle = stringResource(R.string.new_tag)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp, horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(R.string.create_new_tag).uppercase(),
                fontSize = superSmallTextSize,
                color = blackColor,
                fontFamily = semiBoldFont,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = lightBlueColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = viewModel.newTagName,
                        onValueChange = { viewModel.updateNewTag(it) },
                        label = { Text(text = stringResource(R.string.tag_name_dots)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = lightBlueColor,
                            unfocusedContainerColor = lightBlueColor,
                            focusedIndicatorColor = lightBlueColor,
                            unfocusedIndicatorColor = lightBlueColor,
                            focusedLabelColor = lightGreyColor,
                            unfocusedLabelColor = lightGreyColor
                        )
                    )

                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(primaryColor)
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_plus_light_grey),
                            contentDescription = "Plus",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.onPlusButtonClick() },
                            colorFilter = ColorFilter.tint(whiteColor)
                        )
                    }
                }
            }

            if (isBusy == true) {
                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(32.dp),
                        color = primaryColor,
                        trackColor = lightBlueColor,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = viewModel.recentTagsTitle,
                fontSize = superSmallTextSize,
                color = blackColor,
                fontFamily = semiBoldFont,
                modifier = Modifier.align(Alignment.Start)
            )

            FlowRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                recentTags?.let { recentTagsValue ->
                    for (tag in recentTagsValue) {
                        TagView(
                            text = tag.name,
                            isSelected = tag.isSelected.observeAsState(),
                            isDisplayedInNewTagScreen = true,
                            onTagClick = { viewModel.onTagClick(tag) })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.0f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(modifier = Modifier
                    .width(168.dp)
                    .height(48.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = lightBlueColor),
                    onClick = { onCancelBtnClick() }
                ) {
                    Text(
                        text = stringResource(R.string.button_cancel),
                        fontSize = 14.sp,
                        color = primaryColor,
                        fontFamily = regularFont,
                    )
                }

                Button(modifier = Modifier
                    .width(168.dp)
                    .height(48.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    onClick = { viewModel.onAddTagsButtonClick() }
                ) {
                    Text(
                        text = viewModel.addButtonTitle,
                        fontSize = 14.sp,
                        fontFamily = regularFont,
                    )
                }
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
}

@Composable
fun BottomSheetHeader(
    icon: Painter, screenTitle: String
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val subTitleTextSize = 16.sp
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))

    Row(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .background(primaryColor)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Image(
            painter = icon,
            contentDescription = "Tag",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(whiteColor)
        )

        Text(
            text = screenTitle,
            fontSize = subTitleTextSize,
            fontFamily = boldFont,
            color = whiteColor
        )
    }
}