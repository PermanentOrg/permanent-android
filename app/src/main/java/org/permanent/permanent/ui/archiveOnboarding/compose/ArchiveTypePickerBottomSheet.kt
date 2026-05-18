@file:OptIn(ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.ui.composeComponents.MenuItem

fun archiveTypePickerItems(): List<UIArchive> = listOf(
    UIArchive(ArchiveType.PERSON, R.drawable.ic_heart_white, R.string.personal, R.string.personal_description),
    UIArchive(ArchiveType.PERSON, R.drawable.ic_account_empty_primary, R.string.individual, R.string.individual_description),
    UIArchive(ArchiveType.FAMILY, R.drawable.ic_family_primary, R.string.family, R.string.family_description),
    UIArchive(ArchiveType.FAMILY, R.drawable.ic_family_history_primary, R.string.family_history, R.string.family_history_description),
    UIArchive(ArchiveType.FAMILY, R.drawable.ic_community_primary, R.string.community, R.string.community_description),
    UIArchive(ArchiveType.ORGANIZATION, R.drawable.ic_organization_empty_primary, R.string.organization, R.string.organization_description),
    UIArchive(ArchiveType.OTHER, R.drawable.ic_other_primary, R.string.other, R.string.other_description),
    UIArchive(ArchiveType.UNSURE, R.drawable.ic_unsure_primary, R.string.unsure, R.string.unsure_description),
)

@Composable
fun ArchiveTypePickerBottomSheet(
    isTablet: Boolean = false,
    selected: UIArchive? = null,
    items: List<UIArchive>,
    onSelect: (UIArchive) -> Unit,
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
    headerPadding: PaddingValues = PaddingValues(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        ArchiveTypePickerContent(
            isTablet = isTablet,
            selected = selected,
            items = items,
            onSelect = onSelect,
            onDismiss = onDismiss,
            onBack = onBack,
            headerPadding = headerPadding,
            sheetState = sheetState
        )
    }
}

@Composable
fun ColumnScope.ArchiveTypePickerContent(
    isTablet: Boolean = false,
    selected: UIArchive? = null,
    items: List<UIArchive>,
    onSelect: (UIArchive) -> Unit,
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
    headerPadding: PaddingValues = PaddingValues(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    sheetState: SheetState
) {
    val blue900Color = colorResource(R.color.blue900)
    val boldFont = FontFamily(Font(R.font.open_sans_bold_ttf))
    val scope = rememberCoroutineScope()

    val blue200Color = colorResource(R.color.blue200)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(headerPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_back_blue),
                contentDescription = stringResource(R.string.back),
                colorFilter = ColorFilter.tint(blue900Color),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() },
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_close_middle_grey),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Transparent),
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = 8.dp),
            text = stringResource(R.string.archive_type),
            textAlign = TextAlign.Center,
            color = blue900Color,
            fontFamily = boldFont,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Image(
            painter = painterResource(id = R.drawable.ic_close_middle_grey),
            contentDescription = stringResource(R.string.menu_toolbar_close),
            colorFilter = ColorFilter.tint(blue200Color),
            modifier = Modifier
                .size(18.dp)
                .clickable {
                    scope
                        .launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) onDismiss()
                        }
                },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f),
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            val isSelected = selected != null &&
                selected.type == item.type &&
                selected.title == item.title

            HorizontalDivider(color = colorResource(R.color.blue50))

            MenuItem(
                isTablet = isTablet,
                iconResource = painterResource(id = item.icon),
                title = stringResource(item.title),
                subtitle = stringResource(item.description),
                isSelected = isSelected
            ) {
                onSelect(item)
            }
        }
    }
}
