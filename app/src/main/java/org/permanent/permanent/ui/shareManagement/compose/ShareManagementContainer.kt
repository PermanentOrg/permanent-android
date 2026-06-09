package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.AnimatedTemporarySnackbar
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun ShareManagementContainer(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val isBusyState by viewModel.isBusyState.collectAsState()
    val isRefreshingShares by viewModel.isRefreshingShares.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = SharePage.SHARE_ITEM.value, pageCount = { SharePage.entries.size })
    val navigateToPage by viewModel.navigateToPage.collectAsState()

    LaunchedEffect(navigateToPage) {
        navigateToPage?.let { page ->
            pagerState.animateScrollToPage(page.value)
            viewModel.clearPageNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight(0.95f)
            .background(colorResource(id = R.color.blue900))
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState, beyondViewportPageCount = 3, userScrollEnabled = false
            ) { page ->
                when (page) {
                    SharePage.SHARE_ITEM.value -> {
                        ShareItemPage(
                            viewModel = viewModel, onClose = { onClose() })
                    }

                    SharePage.LINK_SETTINGS.value -> {
                        LinkSettingsPage(
                            viewModel = viewModel, onClose = { onClose() })
                    }

                    SharePage.GENERAL_ACCESS.value -> {
                        GeneralAccessPage(
                            viewModel = viewModel, onClose = { onClose() })
                    }

                    SharePage.ACCESS_ROLES.value -> {
                        AccessRolesPage(
                            viewModel = viewModel, onClose = { onClose() })
                    }

                    SharePage.ARCHIVE_ACCESS.value -> {
                        ArchiveAccessPage(
                            viewModel = viewModel, onClose = { onClose() })
                    }

                    SharePage.FIND_ARCHIVE_BY_EMAIL.value -> {
                        FindArchiveByEmailPage(
                            viewModel = viewModel,
                            isCurrentPage = pagerState.settledPage == page,
                            onClose = { onClose() })
                    }

                    SharePage.GRANT_ARCHIVE_ACCESS.value -> {
                        GrantArchiveAccessPage(
                            viewModel = viewModel, onClose = { onClose() })
                    }
                }
            }
        }

        AnimatedTemporarySnackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            type = snackbarType,
            message = snackbarMessage,
            onButtonClick = {
                viewModel.clearSnackbar()
            })
    }

    if (isBusyState || isRefreshingShares) {
        CircularProgressIndicator(modifier = Modifier.fillMaxHeight(0.95f))
    }
}

enum class SharePage(val value: Int) {
    SHARE_ITEM(0), LINK_SETTINGS(1), GENERAL_ACCESS(2), ACCESS_ROLES(3), ARCHIVE_ACCESS(4),
    FIND_ARCHIVE_BY_EMAIL(5), GRANT_ARCHIVE_ACCESS(6);
}
