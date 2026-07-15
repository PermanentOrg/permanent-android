package org.permanent.permanent.ui.fileView

/**
 * Progressive loading states of the full-screen image viewer.
 * The 200 ms cross-dissolve (S3 in Figma) is an animated transition into [Sharp],
 * not a discrete state.
 */
sealed interface ImageViewUiState {
    /** S1 — blurred, upscaled thumbnail with a 16% black scrim. */
    data object BlurredThumbnail : ImageViewUiState

    /** S2 — same as S1 plus the white Permanent loader (full-res not ready after 500 ms). */
    data object BlurredThumbnailWithLoader : ImageViewUiState

    /** S4 — full-resolution image, no blur, no scrim, no loader. */
    data object Sharp : ImageViewUiState

    /** S6 — full-res load failed while connected; tappable retry card over the thumbnail. */
    data object LoadFailed : ImageViewUiState

    /** S7 — device offline; offline card, auto-retries when connectivity returns. */
    data object Offline : ImageViewUiState
}
