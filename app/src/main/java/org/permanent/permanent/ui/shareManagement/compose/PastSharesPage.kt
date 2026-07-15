package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.viewmodels.PastSharesUiState
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun PastSharesPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val state by viewModel.pastSharesState.collectAsState()
    val query by viewModel.pastSharesQuery.collectAsState()
    val accessedArchiveIds by viewModel.accessedArchiveIds.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.Top
        ) {

            NavigationHeader(
                title = stringResource(R.string.select_archive_from_past_shares_title),
                onBackBtnClick = {
                    keyboardController?.hide()
                    viewModel.onBackBtnClick(SharePage.PAST_SHARES)
                },
                onCloseClick = {
                    keyboardController?.hide()
                    onClose()
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.blue25))
            ) {
                ArchiveSearchField(
                    value = query,
                    onValueChange = { viewModel.onPastSharesQueryChange(it) },
                    placeholder = stringResource(R.string.filter_archives_hint),
                    elevated = false
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val s = state) {
                    // The loading spinner overlays the whole page (drawn over the root Box
                    // below), consistent with ShareManagementContainer's busy overlay.
                    PastSharesUiState.Loading -> Unit

                    is PastSharesUiState.Error -> CenteredMessage(text = s.message)

                    is PastSharesUiState.Loaded -> {
                        val myArchives = remember(s.myArchives, query) {
                            viewModel.filterPastShareArchives(s.myArchives, query)
                        }
                        val otherArchives = remember(s.otherArchives, query) {
                            viewModel.filterPastShareArchives(s.otherArchives, query)
                        }
                        if (myArchives.isEmpty() && otherArchives.isEmpty()) {
                            EmptyState(isFiltering = query.isNotBlank())
                        } else {
                            ArchiveSectionsList(
                                myArchives = myArchives,
                                otherArchives = otherArchives,
                                accessedArchiveIds = accessedArchiveIds,
                                onArchiveClick = {
                                    keyboardController?.hide()
                                    viewModel.onPastShareArchiveClick(it)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (state == PastSharesUiState.Loading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ArchiveSectionsList(
    myArchives: List<Archive>,
    otherArchives: List<Archive>,
    accessedArchiveIds: Set<Int>,
    onArchiveClick: (Archive) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        // Figma frame 21716:26422: 32 top / 24 bottom padding, 24 gap between items.
        contentPadding = PaddingValues(top = 32.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        archiveSection(
            titleRes = R.string.my_archives,
            archives = myArchives,
            accessedArchiveIds = accessedArchiveIds,
            onArchiveClick = onArchiveClick
        )
        archiveSection(
            titleRes = R.string.other_archives,
            archives = otherArchives,
            accessedArchiveIds = accessedArchiveIds,
            onArchiveClick = onArchiveClick
        )
    }
}

private fun LazyListScope.archiveSection(
    titleRes: Int,
    archives: List<Archive>,
    accessedArchiveIds: Set<Int>,
    onArchiveClick: (Archive) -> Unit,
) {
    if (archives.isEmpty()) {
        return
    }
    item(key = "header-$titleRes") {
        SectionTitle(
            text = stringResource(titleRes),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
    }
    // Archives that already have access are pushed to the bottom (stable within each group).
    val orderedArchives = archives.sortedBy { it.id in accessedArchiveIds }
    items(orderedArchives, key = { it.id }) { archive ->
        ArchiveResultRow(
            archive = archive,
            hasAccess = archive.id in accessedArchiveIds,
            onClick = { onArchiveClick(archive) }
        )
    }
}

@Composable
private fun EmptyState(isFiltering: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                if (isFiltering) R.string.no_archives_match_search_title
                else R.string.no_past_shares_title
            ),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.blue900),
            )
        )

        Text(
            text = stringResource(
                if (isFiltering) R.string.no_archives_match_search_subtitle
                else R.string.no_past_shares_subtitle
            ),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue400),
            )
        )
    }
}

