package org.permanent.permanent.ui.bulkEditMetadata.compose

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.permanent.permanent.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationScreen(
    cancel: () -> Unit
) {
    val context = LocalContext.current
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val primaryColor = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val lightBlueColor = Color(ContextCompat.getColor(context, R.color.superLightBlue))

    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    var isSearching by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Column() {
        BottomSheetHeader(
            painterResource(id = R.drawable.ic_edit_name),
            screenTitle = stringResource(id = R.string.edit_file_names)
        )
        Box(
            contentAlignment = Alignment.BottomStart
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = singapore),
                    title = "Singapore",
                    snippet = "Marker in Singapore"
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MySearchBar(
                    modifier = Modifier
                        .padding(24.dp)
                        .height(48.dp)
                        .background(Color.White)
                        .shadow(
                            elevation = 16.dp,
                            spotColor = Color(0x29000000),
                            ambientColor = Color(0x29000000)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFF4F6FD),
                            shape = RoundedCornerShape(size = 2.dp)
                        ),
                    text = searchText,
                    onTextChange = {
                        searchText = it
                    },
                    onSearch = {
                        // Perform search logic here
                    },
                    isFocused = {

                    },
                    leadingIcon = {
                        Image(
                            painter = painterResource(R.drawable.ic_search_middle_grey),
                            contentDescription = "Search icon"
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = {
                                searchText = ""
                            }) {
                                Image(
                                    painter = painterResource(R.drawable.ic_close_middle_grey),
                                    contentDescription = "Search icon"
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1.0f))

                Row(
                    modifier = Modifier
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(5.dp))
                        .background(Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f)
                            .height(48.dp),
                            shape = RoundedCornerShape(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = lightBlueColor),
                            onClick = {
                                cancel()
                            }
                        ) {
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
//                enabled = !uiState.isBusy,
                            onClick = {
//                    apply()
                            }
                        ) {
//                if(uiState.isBusy) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.width(32.dp),
//                        color = primaryColor,
//                        trackColor = lightBlueColor,
//                    )
//                } else {
                            Text(
                                text = stringResource(R.string.set_location),
                                fontSize = 14.sp,
                                fontFamily = regularFont,
                            )
//                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationDetails() {
    Row() {
        Icon(
            imageVector = Icons.Default.Search,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = null
        )
        Column() {
            Text(
                text = "Norwich",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF606060),
                )
            )
            Text(
                text = "12 km  •  Meeting House Ln, Oakdale, CT 06370, USA",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFFB4B4B4),
                )
            )
        }
    }
}

@Composable
fun MySearchBar(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    isFocused: (Boolean) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f)
                .onFocusChanged {
                    isFocused(it.isFocused)
                },
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart // Align text to the start
                ) {
                    if (text.isEmpty()) {
                        Text(text = "Search...", color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )

        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}