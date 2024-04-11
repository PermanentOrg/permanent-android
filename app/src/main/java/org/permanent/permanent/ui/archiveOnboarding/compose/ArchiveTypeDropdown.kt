@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.ui.composeComponents.MenuItem

@Composable
fun ArchiveTypeDropdown(
    isTablet: Boolean = false,
    onListItemClick: (archiveType: ArchiveType) -> Unit
) {
    val context = LocalContext.current
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val mardiGrasColor = Color(ContextCompat.getColor(context, R.color.mardiGras))
    val orangeColor = Color(ContextCompat.getColor(context, R.color.orange))
    val blue900Color = Color(ContextCompat.getColor(context, R.color.blue900))
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val semiboldFont = FontFamily(Font(R.font.open_sans_semibold_ttf))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    var listItems = listOf(
        UIArchive(
            ArchiveType.PERSON,
            R.drawable.ic_heart_white,
            R.string.personal,
            R.string.personal_description
        ),
        UIArchive(
            ArchiveType.PERSON,
            R.drawable.ic_account_empty_primary,
            R.string.individual,
            R.string.individual_description
        ),
        UIArchive(
            ArchiveType.FAMILY,
            R.drawable.ic_family_primary,
            R.string.family,
            R.string.family_description
        ),
        UIArchive(
            ArchiveType.FAMILY,
            R.drawable.ic_family_history_primary,
            R.string.family_history,
            R.string.family_history_description
        ),
        UIArchive(
            ArchiveType.FAMILY,
            R.drawable.ic_community_primary,
            R.string.community,
            R.string.community_description
        ),
        UIArchive(
            ArchiveType.ORGANIZATION,
            R.drawable.ic_organization_empty_primary,
            R.string.organization,
            R.string.organization_description
        )
    )

    var currentArchiveType by remember { mutableStateOf(listItems[0]) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Button(modifier = Modifier
        .fillMaxWidth()
        .height(if (isTablet) 168.dp else 112.dp)
        .background(
            Brush.horizontalGradient(listOf(mardiGrasColor, orangeColor)),
            RoundedCornerShape(12.dp)
        ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        onClick = { showBottomSheet = true }) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(id = currentArchiveType.icon),
                colorFilter = ColorFilter.tint(whiteColor),
                contentDescription = "",
                modifier = Modifier.size(16.dp)
            )

            Column(
                modifier = Modifier.weight(1.0f), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = currentArchiveType.title),
                    color = whiteColor,
                    fontSize = if (isTablet) 18.sp else 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = semiboldFont,
                )

                Text(
                    text = stringResource(id = currentArchiveType.description),
                    color = whiteColor,
                    fontSize = if (isTablet) 18.sp else 12.sp,
                    lineHeight = if (isTablet) 32.sp else 16.sp,
                    fontFamily = regularFont,
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_arrow_drop_down),
                colorFilter = ColorFilter.tint(whiteColor),
                contentDescription = "Drop down",
                modifier = Modifier.size(12.dp)
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                }, sheetState = sheetState
            ) {
                // Sheet header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 20.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_close_middle_grey),
                        contentDescription = "Plus",
                        colorFilter = ColorFilter.tint(Color.Transparent),
                        modifier = Modifier.size(18.dp),
                    )

                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = stringResource(R.string.archive_type),
                        textAlign = TextAlign.Center,
                        color = blue900Color,
                        fontFamily = boldFont,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_close_middle_grey),
                        contentDescription = "Plus",
                        colorFilter = ColorFilter.tint(blue900Color),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showBottomSheet = false
                                        }
                                    }
                            },
                    )
                }
                // Sheet content
                Column(
                    modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Top
                ) {
                    listItems.forEach { item ->
                        Divider()

                        MenuItem(
                            isTablet = isTablet,
                            iconResource = painterResource(id = item.icon),
                            title = stringResource(item.title),
                            subtitle = stringResource(item.description)
                        ) {
                            currentArchiveType = item
                            onListItemClick(item.type)
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                        }
                    }

                    Divider()
                }
            }
        }
    }
}

data class UIArchive(
    var type: ArchiveType,
    var icon: Int,
    var title: Int,
    var description: Int
)

@Preview
@Composable
fun CustomDropdownPreview() {
    ArchiveTypeDropdown(onListItemClick = {})
}
