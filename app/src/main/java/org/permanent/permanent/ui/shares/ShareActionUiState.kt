package org.permanent.permanent.ui.shares

import org.permanent.permanent.models.AccessRole

sealed class ShareActionUiState {
    object SelectArchive : ShareActionUiState()
    object Loading : ShareActionUiState()
    object RequestAccess : ShareActionUiState()
    object AccessRequested : ShareActionUiState()
    data class Approved(val accessRole: AccessRole?) : ShareActionUiState()
    object OwnedByMe : ShareActionUiState()
    object Error : ShareActionUiState()
}
