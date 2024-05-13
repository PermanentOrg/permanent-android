package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
                SearchBar(
                    query = "searchQuery",
                    onQueryChange = {

                    },
                    onSearch = {},
                    placeholder = {
                        Text(text = "Search movies")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {},
                    content = {},
                    active = false,
                    onActiveChange = {},
                    tonalElevation = 0.dp
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