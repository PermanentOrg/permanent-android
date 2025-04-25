package org.permanent.permanent.ui.bulkEditMetadata.compose

import CustomDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.EditLocationViewModel

@Composable
fun EditLocationScreen(
    viewModel: EditLocationViewModel, cancel: () -> Unit
) {
    val context = LocalContext.current
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val superLightBlue = Color(ContextCompat.getColor(context, R.color.blue25))

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(viewModel.selectedLocation.value, 12f)
    }

    val openAlertDialog = remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val locations by viewModel.locations.observeAsState(emptyList())

    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel.searchText.value) {
        delay(500)
        viewModel.fetchLocations(viewModel.searchText.value)
    }

    LaunchedEffect(viewModel.selectedLocation.value) {
        cameraPositionState.position =
            CameraPosition.fromLatLngZoom(viewModel.selectedLocation.value, 12f)
    }

    LaunchedEffect(key1 = viewModel.shouldClose.value, block = {
        if (viewModel.shouldClose.value) {
            cancel()
        }
    })

    Column() {
        BottomSheetHeader(
            painterResource(id = R.drawable.map_icon),
            screenTitle = stringResource(id = R.string.add_location)
        )
        Box(
            contentAlignment = Alignment.BottomStart
        ) {
            if (!isSearching) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState
                ) {
                    if (viewModel.selectedLocation.value != viewModel.defaultPosition) {
                        Marker(
                            state = MarkerState(position = viewModel.selectedLocation.value)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LocationSearchBar(modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        spotColor = Color.Black.copy(0.16f),
                        ambientColor = Color.Black.copy(0.16f)
                    )
                    .border(
                        width = 1.dp,
                        color = superLightBlue,
                        shape = RoundedCornerShape(size = 2.dp)
                    )
                    .height(48.dp)
                    .background(
                        color = Color.White, shape = RoundedCornerShape(size = 2.dp)
                    ), text = viewModel.searchText.value, onTextChange = {
                    viewModel.searchText.value = it
                }, isFocused = {
                    isSearching = it
                }, leadingIcon = {
                    Image(
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp),
                        painter = painterResource(R.drawable.search_icon),
                        contentDescription = "Search",
                        alignment = Alignment.Center,
                        contentScale = ContentScale.None
                    )
                }, trailingIcon = {
                    if (viewModel.searchText.value.isNotEmpty()) {
                        IconButton(modifier = Modifier
                            .height(40.dp)
                            .width(40.dp), onClick = {
                            viewModel.searchText.value = ""
                        }) {
                            Image(
                                painter = painterResource(R.drawable.x_icon),
                                contentDescription = "Delete",
                                alignment = Alignment.Center,
                                contentScale = ContentScale.None
                            )
                        }
                    }
                })

                if (isSearching) {
                    LazyColumn(
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        items(locations) { prediction ->
                            val primaryText = prediction.getPrimaryText(null).toString()
                            val secondaryText = prediction.getSecondaryText(null).toString()

                            LocationDetails(primaryText, secondaryText, didSelect = {
                                viewModel.fetchPlace(prediction)
                                focusManager.clearFocus(true)
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1.0f))

                Row(
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                            .height(48.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = superLightBlue),
                            onClick = {
                                cancel()
                            }) {
                            Text(
                                text = stringResource(R.string.button_cancel),
                                fontSize = 14.sp,
                                color = primaryColor,
                                fontFamily = regularFont,
                            )
                        }

                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                            .height(48.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            onClick = {
                                openAlertDialog.value = true
                            }) {
                            if (viewModel.isBusy.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(32.dp),
                                    color = primaryColor,
                                    trackColor = superLightBlue,
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.set_location),
                                    fontSize = 14.sp,
                                    fontFamily = regularFont,
                                )
                            }
                        }
                    }
                }
            }
        }
        when {
            openAlertDialog.value -> {
                CustomDialog(
                    title = stringResource(id = R.string.location_confirmation_title),
                    subtitle = stringResource(id = R.string.location_confirmation_substring),
                    okButtonText = stringResource(id = R.string.set_location),
                    cancelButtonText = stringResource(id = R.string.button_cancel),
                    onConfirm = {
                        openAlertDialog.value = false
                        viewModel.updateRecordLocation()
                    }) {
                    openAlertDialog.value = false
                }
            }
        }
    }
}