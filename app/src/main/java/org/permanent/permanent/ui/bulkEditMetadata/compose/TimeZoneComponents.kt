package org.permanent.permanent.ui.bulkEditMetadata.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.bulkEditMetadata.ListedZone
import org.permanent.permanent.ui.bulkEditMetadata.TimeZoneOption
import org.permanent.permanent.ui.bulkEditMetadata.TimeZoneRegion
import org.permanent.permanent.viewmodels.EditDateTimeViewModel

private val rowTextStyle = TextStyle(fontSize = 14.sp, lineHeight = 24.sp, fontFamily = regularFont)

// The row under the time box: "GMT+02:00  Romania, Bucharest", "Select time zone", or greyed until a time is typed.
@Composable
internal fun TimeZoneRow(zone: TimeZoneOption?, offsetLabel: String?, enabled: Boolean, onClick: () -> Unit) {
    val blue200 = colorResource(R.color.blue200)
    val blue400 = colorResource(R.color.blue400)
    val blue900 = colorResource(R.color.blue900)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_globe_primary),
            contentDescription = null,
            tint = if (enabled) blue900 else blue200,
            modifier = Modifier.size(24.dp)
        )
        when {
            !enabled -> Text(
                text = stringResource(R.string.date_time_zone_needs_time),
                modifier = Modifier.weight(1f),
                style = rowTextStyle.copy(color = blue200)
            )
            zone != null && offsetLabel != null -> OffsetAndPlace(
                offset = offsetLabel,
                offsetColor = blue900,
                place = zone.placeName,
                placeColor = blue400,
                modifier = Modifier.weight(1f)
            )
            else -> Text(
                text = stringResource(R.string.date_time_select_time_zone),
                modifier = Modifier.weight(1f),
                style = rowTextStyle.copy(color = blue400)
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_angle_right_grey),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun OffsetAndPlace(
    offset: String, offsetColor: Color, place: String, placeColor: Color, modifier: Modifier = Modifier,
    city: String? = null
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = offset, style = rowTextStyle.copy(color = offsetColor), maxLines = 1)
        Text(
            text = buildAnnotatedString {
                if (city != null) {
                    withStyle(SpanStyle(color = placeColor)) { append(place.removeSuffix(city)) }
                    withStyle(SpanStyle(color = placeColor, fontFamily = mediumFont)) { append(city) }
                } else {
                    withStyle(SpanStyle(color = placeColor)) { append(place) }
                }
            },
            style = rowTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// The sheet's second page: Back, search, a pinned "No time zone" row and the zones by region.
@Composable
internal fun TimeZonePicker(viewModel: EditDateTimeViewModel) {
    val blue50 = colorResource(R.color.blue50)
    val blue400 = colorResource(R.color.blue400)
    val blue900 = colorResource(R.color.blue900)
    val chosenId = viewModel.entry.value.zoneId
    val chosen = viewModel.entry.value.zone
    val regions = viewModel.zoneRegions.value
    val query = viewModel.zoneQuery.value
    val unlistedChosen = chosen?.takeIf { option ->
        query.isBlank() && regions.none { region -> region.zones.any { it.option.id == option.id } }
    }
    val listState = rememberLazyListState()
    var didScroll by remember { mutableStateOf(false) }

    BackHandler(onBack = viewModel::onZonePickerBack)

    // Opens on the chosen zone; every new search starts at the top.
    LaunchedEffect(regions) {
        if (regions.isEmpty()) return@LaunchedEffect
        if (didScroll) {
            listState.scrollToItem(0)
            return@LaunchedEffect
        }
        didScroll = true
        regions.chosenIndex(chosenId)?.let { listState.scrollToItem(it, scrollOffset = -200) }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(Color.White)
            .height((LocalConfiguration.current.screenHeightDp * 0.85f).dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SheetCircleButton(R.drawable.ic_arrow_back_blue, stringResource(R.string.back), dark = false, onClick = viewModel::onZonePickerBack)
            Text(
                text = stringResource(R.string.date_time_zone_title),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontFamily = mediumFont, color = blue900)
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        ZoneSearchField(query, viewModel::onZoneQueryChanged)

        if (query.isBlank()) {
            ZoneListRow(selected = chosenId == null, onClick = { viewModel.onZonePicked(null) }, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = stringResource(R.string.date_time_no_zone), style = rowTextStyle.copy(color = blue900))
            }
            HorizontalDivider(color = blue50, modifier = Modifier.padding(horizontal = 16.dp))
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            unlistedChosen?.let { option ->
                item(key = "unlisted") {
                    ZoneOptionRow(ListedZone(option, viewModel.zoneOffsetLabel(option)), viewModel, selected = true)
                }
            }
            regions.forEach { region ->
                item(key = "region-${region.name}") {
                    Text(
                        text = region.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = TextStyle(fontSize = 10.sp, letterSpacing = 1.6.sp, fontFamily = mediumFont, color = blue900)
                    )
                }
                items(region.zones, key = { it.option.id }) { zone ->
                    ZoneOptionRow(zone, viewModel, selected = zone.option.id == chosenId)
                }
            }
            if (regions.isEmpty() && unlistedChosen == null && query.isNotBlank()) {
                item(key = "empty") {
                    Text(
                        text = stringResource(R.string.date_time_no_matching_zones),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = rowTextStyle.copy(color = blue400)
                    )
                }
            }
        }
    }
}

// The chosen zone's position: the "unlisted" item is never in the regions, so it needs no scroll.
private fun List<TimeZoneRegion>.chosenIndex(id: String?): Int? {
    if (id == null) return null
    var index = 0
    forEach { region ->
        index++
        val inRegion = region.zones.indexOfFirst { it.option.id == id }
        if (inRegion >= 0) return index + inRegion
        index += region.zones.size
    }
    return null
}

@Composable
private fun ZoneOptionRow(zone: ListedZone, viewModel: EditDateTimeViewModel, selected: Boolean) {
    val blue400 = colorResource(R.color.blue400)
    val blue900 = colorResource(R.color.blue900)
    ZoneListRow(selected = selected, onClick = { viewModel.onZonePicked(zone.option.id) }) {
        OffsetAndPlace(
            offset = zone.offsetLabel,
            offsetColor = blue400,
            place = zone.option.placeName,
            placeColor = blue900,
            city = zone.option.city,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ZoneListRow(
    selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit
) {
    val blue25 = colorResource(R.color.blue25)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) blue25 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle_green),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ZoneSearchField(query: String, onQueryChange: (String) -> Unit) {
    val blue25 = colorResource(R.color.blue25)
    val blue50 = colorResource(R.color.blue50)
    val blue200 = colorResource(R.color.blue200)
    val blue900 = colorResource(R.color.blue900)
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .border(1.dp, blue50, RoundedCornerShape(8.dp))
            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(blue25),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search_middle_grey),
                contentDescription = null,
                tint = blue900,
                modifier = Modifier.size(16.dp)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(text = stringResource(R.string.date_time_zone_search_hint), style = rowTextStyle.copy(color = blue200), maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = rowTextStyle.copy(color = blue900),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_close_circle),
                contentDescription = stringResource(R.string.date_time_zone_clear_search),
                tint = blue200,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onQueryChange("") }
            )
        }
    }
}
